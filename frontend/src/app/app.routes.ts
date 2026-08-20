import { Routes } from '@angular/router';
import { LeagueListComponent } from './leagues/league-list.component';
import { PowerRankingsComponent } from './rankings/power-rankings.component';
import { TrendChartComponent } from './trends/trend-chart.component';
import { RosterDetailComponent } from './roster/roster-detail.component';

/** Top-level page routes; unmatched paths fall back to the league list. */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'leagues' },
  { path: 'leagues', component: LeagueListComponent },
  { path: 'leagues/:id', component: PowerRankingsComponent },
  { path: 'leagues/:id/trends', component: TrendChartComponent },
  { path: 'rosters/:id', component: RosterDetailComponent },
  { path: '**', redirectTo: 'leagues' }
];
