export interface League {
  id: string;
  name: string;
  season: string;
  totalRosters: number;
  superflex: boolean;
  lastSyncedAt: string | null;
}

export interface RosterSummary {
  rank: number;
  rosterId: number;
  teamName: string;
  managerName: string | null;
  wins: number;
  losses: number;
  pointsFor: number;
  totalValue: number;
  qbValue: number;
  rbValue: number;
  wrValue: number;
  teValue: number;
  avgAge: number | null;
}

export interface PlayerValue {
  playerId: string;
  name: string;
  position: string | null;
  nflTeam: string | null;
  age: number | null;
  injuryStatus: string | null;
  starter: boolean;
  value: number;
}

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

export interface TrendPoint {
  date: string;
  totalValue: number;
}

export interface TrendSeries {
  rosterId: number;
  teamName: string;
  points: TrendPoint[];
}
