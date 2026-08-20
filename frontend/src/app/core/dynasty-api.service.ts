import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { League, RosterDetail, RosterSummary, TrendSeries } from './models';

@Injectable({ providedIn: 'root' })
export class DynastyApiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  getLeagues(): Observable<League[]> {
    return this.http.get<League[]>(`${this.base}/leagues`);
  }

  addLeague(sleeperLeagueId: string): Observable<League> {
    return this.http.post<League>(`${this.base}/leagues`, { sleeperLeagueId });
  }

  getLeague(id: string): Observable<League> {
    return this.http.get<League>(`${this.base}/leagues/${id}`);
  }

  resyncLeague(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/leagues/${id}/sync`, {});
  }

  deleteLeague(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/leagues/${id}`);
  }

  getPowerRankings(leagueId: string): Observable<RosterSummary[]> {
    return this.http.get<RosterSummary[]>(`${this.base}/leagues/${leagueId}/rosters`);
  }

  getRoster(rosterId: number): Observable<RosterDetail> {
    return this.http.get<RosterDetail>(`${this.base}/rosters/${rosterId}`);
  }

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
