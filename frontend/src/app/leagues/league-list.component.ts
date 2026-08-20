import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { ToastService } from '../core/toast.service';
import { League } from '../core/models';

@Component({
  selector: 'app-league-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <h1>Your Leagues</h1>

    <form class="add-league card" (ngSubmit)="addLeague()">
      <label for="league-id">Sleeper league ID</label>
      <div class="add-row">
        <input id="league-id" name="leagueId" [(ngModel)]="leagueId"
               placeholder="e.g. 1180213086679706624" autocomplete="off" />
        <button type="submit" [disabled]="!leagueId.trim() || adding()">
          {{ adding() ? 'Syncing…' : 'Track league' }}
        </button>
      </div>
      <p class="hint">Find it in the Sleeper app URL: sleeper.com/leagues/&lt;league id&gt;</p>
    </form>

    @if (leagues().length === 0 && loaded()) {
      <p class="empty">No leagues tracked yet. Paste your Sleeper league ID above — the first sync
        pulls rosters and takes a few seconds.</p>
    }

    <div class="league-grid">
      @for (league of leagues(); track league.id) {
        <div class="card league-card">
          <div class="league-card-head">
            <a [routerLink]="['/leagues', league.id]" class="league-name">{{ league.name }}</a>
            <span class="pill">{{ league.season }}</span>
            @if (league.superflex) {
              <span class="pill pill-sf">SF</span>
            }
          </div>
          <p class="muted">{{ league.totalRosters }} teams ·
            data as of {{ league.lastSyncedAt ? (league.lastSyncedAt | date: 'MMM d, h:mm a') : 'never' }}</p>
          <div class="league-actions">
            <a [routerLink]="['/leagues', league.id]">Power rankings</a>
            <a [routerLink]="['/leagues', league.id, 'trends']">Trends</a>
            <button class="link" (click)="resync(league)">Re-sync</button>
            <button class="link danger" (click)="remove(league)">Remove</button>
          </div>
        </div>
      }
    </div>
  `
})
export class LeagueListComponent implements OnInit {
  private api = inject(DynastyApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  leagues = signal<League[]>([]);
  loaded = signal(false);
  adding = signal(false);
  leagueId = '';

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.api.getLeagues().subscribe((leagues) => {
      this.leagues.set(leagues);
      this.loaded.set(true);
    });
  }

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

  resync(league: League): void {
    this.api.resyncLeague(league.id).subscribe(() => {
      this.toast.show(`Re-sync of "${league.name}" kicked off — refresh in a few seconds.`);
    });
  }

  remove(league: League): void {
    if (!confirm(`Stop tracking "${league.name}"? Snapshot history will be deleted.`)) {
      return;
    }
    this.api.deleteLeague(league.id).subscribe(() => this.refresh());
  }
}
