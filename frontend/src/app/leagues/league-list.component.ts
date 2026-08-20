import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { ToastService } from '../core/toast.service';
import { League } from '../core/models';

/**
 * Home page. Shows the add-league form (Sleeper league ID input) and a card
 * grid of every tracked league with links to rankings/trends and
 * re-sync / remove actions.
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
  /** True once the first fetch has returned (gates the empty-state message). */
  loaded = signal(false);
  /** True while an add-league request (first sync) is in flight. */
  adding = signal(false);
  /** Bound to the league ID input field. */
  leagueId = '';

  /** On load: fetch the tracked leagues. */
  ngOnInit(): void {
    this.refresh();
  }

  /** Reloads the league list from the API. No input; updates the `leagues` signal. */
  refresh(): void {
    this.api.getLeagues().subscribe((leagues) => {
      this.leagues.set(leagues);
      this.loaded.set(true);
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
      this.toast.show(`Re-sync of "${league.name}" kicked off — refresh in a few seconds.`);
    });
  }

  /**
   * Stops tracking a league (after a confirm dialog) and refreshes the list.
   * @param league the league to delete, along with its snapshot history
   */
  remove(league: League): void {
    if (!confirm(`Stop tracking "${league.name}"? Snapshot history will be deleted.`)) {
      return;
    }
    this.api.deleteLeague(league.id).subscribe(() => this.refresh());
  }
}
