// TypeScript mirrors of the backend's web/dto response records. Keep these
// in sync with the Java DTOs in backend/.../web/dto/ by hand — there's no
// shared schema between the two projects.

/** One tracked Sleeper league. */
export interface League {
  id: string;
  name: string;
  season: string;
  totalRosters: number;
  superflex: boolean;
  /** ISO timestamp of the last successful sync, or null if never synced. */
  lastSyncedAt: string | null;
}

/** One row of the power rankings table: a roster's record and current value. */
export interface RosterSummary {
  rank: number;
  rosterId: number;
  teamName: string;
  managerName: string | null;
  wins: number;
  losses: number;
  pointsFor: number;
  totalValue: number;
  /** Positional value split, used to draw the stacked bar. */
  qbValue: number;
  rbValue: number;
  wrValue: number;
  teValue: number;
  avgAge: number | null;
}

/** One player's computed value within a roster. */
export interface PlayerValue {
  playerId: string;
  name: string;
  position: string | null;
  nflTeam: string | null;
  age: number | null;
  /** e.g. "Questionable", "IR", or null if healthy. */
  injuryStatus: string | null;
  starter: boolean;
  value: number;
}

/** Full roster detail: header info plus every player's value. */
export interface RosterDetail {
  rosterId: number;
  leagueId: string;
  leagueName: string;
  teamName: string;
  managerName: string | null;
  wins: number;
  losses: number;
  pointsFor: number;
  totalValue: number;
  players: PlayerValue[];
}

/** One day's snapshot of a roster's total value, for the trend chart. */
export interface TrendPoint {
  date: string;
  totalValue: number;
}

/** A roster's full value history — one line on the trend chart. */
export interface TrendSeries {
  rosterId: number;
  teamName: string;
  points: TrendPoint[];
}
