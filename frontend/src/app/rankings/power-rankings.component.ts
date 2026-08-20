import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { League, RosterSummary } from '../core/models';

/** Columns the rankings table can be sorted by. */
type SortKey = 'rank' | 'teamName' | 'wins' | 'pointsFor' | 'totalValue' | 'avgAge';

/**
 * Power rankings page for one league: a sortable table of every roster with
 * record, points-for, total value, a stacked positional-value bar, and
 * average age. Rows link through to the roster detail page.
 */
@Component({
  selector: 'app-power-rankings',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './power-rankings.component.html',
  styleUrl: './power-rankings.component.css'
})
export class PowerRankingsComponent implements OnInit {
  private api = inject(DynastyApiService);
  private route = inject(ActivatedRoute);

  /** The league being viewed (header info). */
  league = signal<League | null>(null);
  /** Ranking rows as returned by the API (already ranked by value). */
  rows = signal<RosterSummary[]>([]);
  /** Current sort column. */
  sortKey = signal<SortKey>('rank');
  /** Sort direction; true = ascending. */
  sortAsc = signal(true);

  /** The rows re-sorted by the active column/direction. Output: a new sorted array. */
  sorted = computed(() => {
    const key = this.sortKey();
    const asc = this.sortAsc() ? 1 : -1;
    return [...this.rows()].sort((a, b) => {
      const av = a[key] ?? 0;
      const bv = b[key] ?? 0;
      return (av < bv ? -1 : av > bv ? 1 : 0) * asc;
    });
  });

  /** Largest total value in the league — scales the positional bars. */
  private maxValue = computed(() => Math.max(1, ...this.rows().map((r) => r.totalValue)));

  /** On load: fetch the league header and its ranked rosters (id from the route). */
  ngOnInit(): void {
    const leagueId = this.route.snapshot.paramMap.get('id')!;
    this.api.getLeague(leagueId).subscribe((league) => this.league.set(league));
    this.api.getPowerRankings(leagueId).subscribe((rows) => this.rows.set(rows));
  }

  /**
   * Header click handler: sorts by the given column, or flips the direction
   * if that column is already active.
   * @param key the column that was clicked
   */
  sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortAsc.update((v) => !v);
    } else {
      this.sortKey.set(key);
      this.sortAsc.set(key === 'rank' || key === 'teamName');
    }
  }

  /**
   * Width of a row's positional bar relative to the league leader.
   * @param row the ranking row
   * @returns a percentage in (0, 100]
   */
  barWidth(row: RosterSummary): number {
    return (row.totalValue / this.maxValue()) * 100;
  }
}
