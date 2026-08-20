import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { ToastService } from '../core/toast.service';
import { League, RosterSummary } from '../core/models';

/**
 * Home page. A hero with the add-league form, a summary strip of what's
 * being tracked, and a card per league showing its current leader. Falls
 * back to a "how it works" walkthrough when nothing is tracked yet.
 */
@Component({
  selector: 'app-league-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './league-list.component.html',
  styleUrl: './league-list.component.css'
})
export class LeagueListComponent implements OnInit {
  private api = inject(DynastyApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  /** Leagues currently tracked by the backend. */
  leagues = signal<League[]>([]);
  /** Current top-ranked roster per league ID, for the card previews. */
  leaders = signal<Record<string, RosterSummary>>({});
  /** True once the first fetch has returned (gates the empty state). */
  loaded = signal(false);
  /** True while an add-league request (first sync) is in flight. */
  adding = signal(false);
  /** League awaiting delete confirmation; null when the dialog is closed. */
  pendingRemoval = signal<League | null>(null);
  /** True while the delete request is in flight. */
  removing = signal(false);
  /** Bound to the league ID input field. */
  leagueId = '';

  /** Total teams across every tracked league — the middle summary stat. */
  totalTeams = computed(() =>
    this.leagues().reduce((sum, league) => sum + league.totalRosters, 0)
  );

  /** Seasons covered, e.g. "2025" or "2024–2025". */
  seasonLabel = computed(() => {
    const seasons = [...new Set(this.leagues().map((l) => l.season))].sort();
    if (seasons.length === 0) {
      return '—';
    }
    return seasons.length === 1 ? seasons[0] : `${seasons[0]}–${seasons[seasons.length - 1]}`;
  });

  /** On load: fetch the tracked leagues. */
  ngOnInit(): void {
    this.refresh();
  }

  /** Reloads the league list, then each league's current leader. */
  refresh(): void {
    this.api.getLeagues().subscribe((leagues) => {
      this.leagues.set(leagues);
      this.loaded.set(true);
      leagues.forEach((league) => this.loadLeader(league.id));
    });
  }

  /**
   * Fetches one league's rankings just to show its current #1 on the card.
   * A league that has never synced returns no rows, in which case the card
   * simply omits the leader line.
   * @param leagueId the league to preview
   */
  private loadLeader(leagueId: string): void {
    this.api.getPowerRankings(leagueId).subscribe((rankings) => {
      if (rankings.length > 0) {
        this.leaders.update((all) => ({ ...all, [leagueId]: rankings[0] }));
      }
    });
  }

  /**
   * Submits the form: tracks the league ID currently in the input and runs
   * its first sync. On success navigates to that league's power rankings;
   * on failure the interceptor shows a toast and the form is re-enabled.
   */
  addLeague(): void {
    const id = this.leagueId.trim();
    if (!id) {
      return;
    }
    this.adding.set(true);
    this.api.addLeague(id).subscribe({
      next: (league) => {
        this.adding.set(false);
        this.leagueId = '';
        this.router.navigate(['/leagues', league.id]);
      },
      error: () => this.adding.set(false)
    });
  }

  /**
   * Kicks off an async re-sync for one league and shows a confirmation toast.
   * @param league the league card the user clicked
   */
  resync(league: League): void {
    this.api.resyncLeague(league.id).subscribe(() => {
      this.toast.success(`Re-sync of "${league.name}" started — refresh in a few seconds.`);
    });
  }

  /**
   * Opens the in-app confirmation dialog for removing a league. Nothing is
   * deleted until {@link confirmRemove} runs.
   * @param league the league the user clicked "Remove" on
   */
  askRemove(league: League): void {
    this.pendingRemoval.set(league);
  }

  /** Closes the confirmation dialog without deleting anything. */
  cancelRemove(): void {
    this.pendingRemoval.set(null);
  }

  /**
   * Deletes the league awaiting confirmation, along with its snapshot
   * history, then closes the dialog and refreshes the list.
   */
  confirmRemove(): void {
    const league = this.pendingRemoval();
    if (!league) {
      return;
    }
    this.removing.set(true);
    this.api.deleteLeague(league.id).subscribe({
      next: () => {
        this.removing.set(false);
        this.pendingRemoval.set(null);
        this.toast.success(`Stopped tracking "${league.name}".`);
        this.refresh();
      },
      error: () => {
        this.removing.set(false);
        this.pendingRemoval.set(null);
      }
    });
  }

  /** Escape closes the dialog, matching what people expect from a modal. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.pendingRemoval() && !this.removing()) {
      this.cancelRemove();
    }
  }
}
