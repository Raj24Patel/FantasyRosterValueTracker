import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { League, RosterDetail, RosterSummary, TrendSeries } from './models';

/**
 * Thin typed wrapper around the backend REST API. Every page injects this
 * instead of calling HttpClient directly, so the endpoint paths and response
 * shapes live in exactly one place.
 */
@Injectable({ providedIn: 'root' })
export class DynastyApiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  /** Input: none. Output: every league currently tracked by the backend. */
  getLeagues(): Observable<League[]> {
    return this.http.get<League[]>(`${this.base}/leagues`);
  }

  /**
   * Starts tracking a league and runs its first sync.
   * @param sleeperLeagueId the numeric Sleeper league ID
   * @returns the newly tracked league
   */
  addLeague(sleeperLeagueId: string): Observable<League> {
    return this.http.post<League>(`${this.base}/leagues`, { sleeperLeagueId });
  }

  /**
   * @param id the tracked league's ID
   * @returns that league's header info, including `lastSyncedAt`
   */
  getLeague(id: string): Observable<League> {
    return this.http.get<League>(`${this.base}/leagues/${id}`);
  }

  /**
   * Kicks off an async re-sync; the response doesn't wait for it to finish.
   * @param id the league to re-sync
   */
  resyncLeague(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/leagues/${id}/sync`, {});
  }

  /** @param id the league to stop tracking (deletes its snapshot history too) */
  deleteLeague(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/leagues/${id}`);
  }

  /**
   * @param leagueId the league to rank
   * @returns every roster in the league, ordered by current total value
   */
  getPowerRankings(leagueId: string): Observable<RosterSummary[]> {
    return this.http.get<RosterSummary[]>(`${this.base}/leagues/${leagueId}/rosters`);
  }

  /**
   * @param rosterId the roster to look up
   * @returns roster detail with a per-player value breakdown
   */
  getRoster(rosterId: number): Observable<RosterDetail> {
    return this.http.get<RosterDetail>(`${this.base}/rosters/${rosterId}`);
  }

  /**
   * @param leagueId the league whose value history to fetch
   * @param from optional ISO date lower bound (inclusive)
   * @param to optional ISO date upper bound (inclusive)
   * @returns one value-over-time series per roster, for the trend chart
   */
  getTrends(leagueId: string, from?: string, to?: string): Observable<TrendSeries[]> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    return this.http.get<TrendSeries[]>(`${this.base}/leagues/${leagueId}/trends`, { params });
  }
}
