import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DynastyApiService } from '../core/dynasty-api.service';
import { RosterDetail } from '../core/models';

/**
 * Roster detail page: one team's header (record, points-for, total value)
 * and its full player table sorted by value, with starter dots and
 * injury badges.
 */
@Component({
  selector: 'app-roster-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './roster-detail.component.html',
  styleUrl: './roster-detail.component.css'
})
export class RosterDetailComponent implements OnInit {
  private api = inject(DynastyApiService);
  private route = inject(ActivatedRoute);

  /** The roster being viewed, including its valued player list. */
  roster = signal<RosterDetail | null>(null);

  /** On load: fetch the roster detail (id from the route). */
  ngOnInit(): void {
    const rosterId = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getRoster(rosterId).subscribe((roster) => this.roster.set(roster));
  }
}
