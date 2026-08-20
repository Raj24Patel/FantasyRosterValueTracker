import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { League, RosterSummary } from '../core/models';

type SortKey = 'rank' | 'teamName' | 'wins' | 'pointsFor' | 'totalValue' | 'avgAge';

@Component({
  selector: 'app-power-rankings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    @if (league(); as lg) {
      <div class="page-head">
        <div>
          <h1>{{ lg.name }}</h1>
          <p class="muted">{{ lg.season }} · {{ lg.totalRosters }} teams
            @if (lg.superflex) { · superflex }
            · data as of {{ lg.lastSyncedAt | date: 'MMM d, h:mm a' }}</p>
        </div>
        <a [routerLink]="['/leagues', lg.id, 'trends']" class="button-link">View trends →</a>
      </div>
    }

    <div class="card table-card">
      <table class="rankings" data-testid="rankings-table">
        <thead>
          <tr>
            <th (click)="sortBy('rank')" class="sortable">#</th>
            <th (click)="sortBy('teamName')" class="sortable">Team</th>
            <th (click)="sortBy('wins')" class="sortable">Record</th>
            <th (click)="sortBy('pointsFor')" class="sortable num">PF</th>
            <th (click)="sortBy('totalValue')" class="sortable num">Value</th>
            <th class="bar-col">Positional breakdown</th>
            <th (click)="sortBy('avgAge')" class="sortable num">Avg age</th>
          </tr>
        </thead>
        <tbody>
          @for (row of sorted(); track row.rosterId) {
            <tr [routerLink]="['/rosters', row.rosterId]" class="clickable">
              <td class="rank">{{ row.rank }}</td>
              <td>
                <div class="team">{{ row.teamName }}</div>
                @if (row.managerName) {
                  <div class="muted small">{{ row.managerName }}</div>
                }
              </td>
              <td>{{ row.wins }}–{{ row.losses }}</td>
              <td class="num">{{ row.pointsFor | number: '1.0-0' }}</td>
              <td class="num value">{{ row.totalValue | number: '1.0-0' }}</td>
              <td class="bar-col">
                <div class="stack" [style.width.%]="barWidth(row)">
                  <span class="seg qb" [style.flex-grow]="row.qbValue" title="QB {{ row.qbValue | number: '1.0-0' }}"></span>
                  <span class="seg rb" [style.flex-grow]="row.rbValue" title="RB {{ row.rbValue | number: '1.0-0' }}"></span>
                  <span class="seg wr" [style.flex-grow]="row.wrValue" title="WR {{ row.wrValue | number: '1.0-0' }}"></span>
                  <span class="seg te" [style.flex-grow]="row.teValue" title="TE {{ row.teValue | number: '1.0-0' }}"></span>
                </div>
              </td>
              <td class="num">{{ row.avgAge ?? '—' }}</td>
            </tr>
          }
        </tbody>
      </table>
      <div class="legend">
        <span><i class="dot qb"></i>QB</span>
        <span><i class="dot rb"></i>RB</span>
        <span><i class="dot wr"></i>WR</span>
        <span><i class="dot te"></i>TE</span>
      </div>
    </div>
  `
})
export class PowerRankingsComponent implements OnInit {
  private api = inject(DynastyApiService);
  private route = inject(ActivatedRoute);

  league = signal<League | null>(null);
  rows = signal<RosterSummary[]>([]);
  sortKey = signal<SortKey>('rank');
  sortAsc = signal(true);

  sorted = computed(() => {
    const key = this.sortKey();
    const asc = this.sortAsc() ? 1 : -1;
    return [...this.rows()].sort((a, b) => {
      const av = a[key] ?? 0;
      const bv = b[key] ?? 0;
      return (av < bv ? -1 : av > bv ? 1 : 0) * asc;
    });
  });

  private maxValue = computed(() => Math.max(1, ...this.rows().map((r) => r.totalValue)));

  ngOnInit(): void {
    const leagueId = this.route.snapshot.paramMap.get('id')!;
    this.api.getLeague(leagueId).subscribe((league) => this.league.set(league));
    this.api.getPowerRankings(leagueId).subscribe((rows) => this.rows.set(rows));
  }

  sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortAsc.update((v) => !v);
    } else {
      this.sortKey.set(key);
      this.sortAsc.set(key === 'rank' || key === 'teamName');
    }
  }

  barWidth(row: RosterSummary): number {
    return (row.totalValue / this.maxValue()) * 100;
  }
}
