import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { RosterDetail } from '../core/models';

@Component({
  selector: 'app-roster-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    @if (roster(); as r) {
      <div class="page-head">
        <div>
          <h1 data-testid="roster-team-name">{{ r.teamName }}</h1>
          <p class="muted">
            <a [routerLink]="['/leagues', r.leagueId]">{{ r.leagueName }}</a>
            · {{ r.wins }}–{{ r.losses }} · {{ r.pointsFor | number: '1.0-0' }} PF
            @if (r.managerName) { · managed by {{ r.managerName }} }
          </p>
        </div>
        <div class="total-badge">
          <span class="label">Roster value</span>
          <span class="value">{{ r.totalValue | number: '1.0-0' }}</span>
        </div>
      </div>

      <div class="card table-card">
        <table class="players" data-testid="players-table">
          <thead>
            <tr>
              <th>Player</th>
              <th>Pos</th>
              <th>Team</th>
              <th class="num">Age</th>
              <th></th>
              <th class="num">Value</th>
            </tr>
          </thead>
          <tbody>
            @for (player of r.players; track player.playerId) {
              <tr>
                <td>
                  <span class="team">{{ player.name }}</span>
                  @if (player.starter) {
                    <span class="starter" title="Starter">●</span>
                  }
                </td>
                <td><span class="pos-tag" [attr.data-pos]="player.position">{{ player.position ?? '—' }}</span></td>
                <td>{{ player.nflTeam ?? 'FA' }}</td>
                <td class="num">{{ player.age ?? '—' }}</td>
                <td>
                  @if (player.injuryStatus) {
                    <span class="injury" [class.severe]="player.injuryStatus === 'IR' || player.injuryStatus === 'Out'">
                      {{ player.injuryStatus }}
                    </span>
                  }
                </td>
                <td class="num value">{{ player.value | number: '1.1-1' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `
})
export class RosterDetailComponent implements OnInit {
  private api = inject(DynastyApiService);
  private route = inject(ActivatedRoute);

  roster = signal<RosterDetail | null>(null);

  ngOnInit(): void {
    const rosterId = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getRoster(rosterId).subscribe((roster) => this.roster.set(roster));
  }
}
