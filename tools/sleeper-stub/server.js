// Tiny stub of the Sleeper API for e2e tests and local demos.
// Serves one 8-team superflex league with a realistic player pool so the
// Playwright run (and CI) never depends on api.sleeper.app being reachable.
//
//   node server.js          → listens on :8080, same paths as the real API (/v1/...)

const http = require('http');

const LEAGUE_ID = '1264349217897840640';

const league = {
  league_id: LEAGUE_ID,
  name: 'The Gridiron Gentlemen',
  season: '2025',
  status: 'complete',
  total_rosters: 8,
  roster_positions: ['QB', 'RB', 'RB', 'WR', 'WR', 'WR', 'TE', 'FLEX', 'SUPER_FLEX', 'BN', 'BN', 'BN', 'BN', 'BN'],
  scoring_settings: { rec: 1.0, pass_td: 4.0, rush_td: 6.0 }
};

const users = [
  ['u1001', 'raj_patel', 'Motor City Misfits'],
  ['u1002', 'bengalboy88', 'Who Dey Dynasty'],
  ['u1003', 'buffalo_phil', "Allen's Army"],
  ['u1004', 'birdgang_pete', 'Brotherly Shove'],
  ['u1005', 'tk_chiefsfan', 'Mahomes Alone'],
  ['u1006', 'htown_hero', 'Stroud Control'],
  ['u1007', 'truzz_tony', 'Truzz']
].map(([user_id, display_name, team_name]) => ({
  user_id,
  display_name,
  metadata: { team_name }
}));

// [id, name, pos, team, age, years_exp, search_rank, injury_status]
const playerRows = [
  ['4046', 'Patrick Mahomes', 'QB', 'KC', 30, 9, 15, null],
  ['4984', 'Josh Allen', 'QB', 'BUF', 30, 8, 6, null],
  ['6770', 'Joe Burrow', 'QB', 'CIN', 29, 6, 10, null],
  ['4881', 'Lamar Jackson', 'QB', 'BAL', 29, 8, 8, null],
  ['11566', 'Jayden Daniels', 'QB', 'WAS', 25, 2, 5, null],
  ['6797', 'Justin Herbert', 'QB', 'LAC', 28, 6, 22, null],
  ['9758', 'C.J. Stroud', 'QB', 'HOU', 24, 3, 30, null],
  ['7527', 'Jalen Hurts', 'QB', 'PHI', 28, 6, 12, null],
  ['11560', 'Caleb Williams', 'QB', 'CHI', 24, 2, 28, null],
  ['6768', 'Jordan Love', 'QB', 'GB', 27, 6, 35, null],
  ['11559', 'Drake Maye', 'QB', 'NE', 24, 2, 26, null],
  ['5849', 'Kyler Murray', 'QB', 'ARI', 29, 7, 45, null],

  ['9509', 'Bijan Robinson', 'RB', 'ATL', 24, 3, 2, null],
  ['9510', 'Jahmyr Gibbs', 'RB', 'DET', 24, 3, 3, null],
  ['4866', 'Saquon Barkley', 'RB', 'PHI', 29, 8, 18, null],
  ['8155', 'Breece Hall', 'RB', 'NYJ', 25, 4, 25, null],
  ['9226', "De'Von Achane", 'RB', 'MIA', 24, 3, 16, null],
  ['6813', 'Jonathan Taylor', 'RB', 'IND', 27, 6, 24, null],
  ['3198', 'Derrick Henry', 'RB', 'BAL', 32, 10, 40, null],
  ['4034', 'Christian McCaffrey', 'RB', 'SF', 30, 9, 38, 'Questionable'],
  ['8228', 'Kyren Williams', 'RB', 'LAR', 25, 4, 33, null],
  ['8210', 'James Cook', 'RB', 'BUF', 26, 4, 29, null],
  ['11584', 'Bucky Irving', 'RB', 'TB', 24, 2, 20, null],
  ['4988', 'Josh Jacobs', 'RB', 'GB', 28, 7, 27, null],
  ['8151', 'Kenneth Walker', 'RB', 'SEA', 25, 4, 48, null],
  ['9998', 'Chase Brown', 'RB', 'CIN', 26, 3, 42, null],
  ['12527', 'Omarion Hampton', 'RB', 'LAC', 23, 1, 21, null],
  ['12526', 'Ashton Jeanty', 'RB', 'LV', 22, 1, 7, null],

  ['7564', "Ja'Marr Chase", 'WR', 'CIN', 26, 5, 1, null],
  ['6794', 'Justin Jefferson', 'WR', 'MIN', 27, 6, 4, null],
  ['6786', 'CeeDee Lamb', 'WR', 'DAL', 27, 6, 9, null],
  ['7547', 'Amon-Ra St. Brown', 'WR', 'DET', 26, 5, 11, null],
  ['11563', 'Malik Nabers', 'WR', 'NYG', 23, 2, 13, 'Questionable'],
  ['9997', 'Puka Nacua', 'WR', 'LAR', 25, 3, 14, null],
  ['11561', 'Marvin Harrison', 'WR', 'ARI', 24, 2, 19, null],
  ['6819', 'Nico Collins', 'WR', 'HOU', 27, 5, 17, null],
  ['11565', 'Brian Thomas', 'WR', 'JAX', 23, 2, 23, null],
  ['8121', 'Drake London', 'WR', 'ATL', 25, 4, 31, null],
  ['5859', 'A.J. Brown', 'WR', 'PHI', 29, 7, 32, null],
  ['8137', 'Garrett Wilson', 'WR', 'NYJ', 26, 4, 34, null],
  ['11562', 'Rome Odunze', 'WR', 'CHI', 24, 2, 37, null],
  ['11575', 'Ladd McConkey', 'WR', 'LAC', 24, 2, 36, null],
  ['6801', 'Tee Higgins', 'WR', 'CIN', 27, 6, 41, null],
  ['5846', 'DK Metcalf', 'WR', 'PIT', 28, 7, 44, null],
  ['9754', 'Jaxon Smith-Njigba', 'WR', 'SEA', 24, 3, 39, null],
  ['3163', 'Tyreek Hill', 'WR', 'MIA', 32, 10, 50, 'Out'],
  ['2133', 'Davante Adams', 'WR', 'LAR', 33, 12, 55, null],
  ['7525', 'DeVonta Smith', 'WR', 'PHI', 27, 5, 43, null],
  ['9488', 'Jordan Addison', 'WR', 'MIN', 24, 3, 47, null],
  ['9500', 'Zay Flowers', 'WR', 'BAL', 25, 3, 46, null],
  ['8134', 'George Pickens', 'WR', 'DAL', 25, 4, 49, null],
  ['8144', 'Chris Olave', 'WR', 'NO', 26, 4, 52, 'Questionable'],

  ['11596', 'Brock Bowers', 'TE', 'LV', 23, 2, 57, null],
  ['9484', 'Sam LaPorta', 'TE', 'DET', 25, 3, 60, null],
  ['8130', 'Trey McBride', 'TE', 'ARI', 26, 4, 58, null],
  ['4217', 'George Kittle', 'TE', 'SF', 32, 9, 65, null],
  ['1466', 'Travis Kelce', 'TE', 'KC', 36, 13, 80, null],
  ['4973', 'Mark Andrews', 'TE', 'BAL', 31, 8, 85, null],
  ['9486', 'Dalton Kincaid', 'TE', 'BUF', 26, 3, 90, null],
  ['5844', 'T.J. Hockenson', 'TE', 'MIN', 28, 7, 88, null],
  ['3214', 'Evan Engram', 'TE', 'DEN', 31, 9, 95, null],
  ['9481', 'Tucker Kraft', 'TE', 'GB', 25, 3, 70, null],
  ['7553', 'Kyle Pitts', 'TE', 'ATL', 25, 5, 75, null],
  ['5857', 'David Njoku', 'TE', 'CLE', 30, 9, 92, null],

  ['DET', 'Detroit Lions', 'DEF', 'DET', null, null, 320, null],
  ['SF', 'San Francisco 49ers', 'DEF', 'SF', null, null, 340, null],
  ['BAL', 'Baltimore Ravens', 'DEF', 'BAL', null, null, 325, null],
  ['PHI', 'Philadelphia Eagles', 'DEF', 'PHI', null, null, 315, null],
  ['KC', 'Kansas City Chiefs', 'DEF', 'KC', null, null, 345, null],
  ['BUF', 'Buffalo Bills', 'DEF', 'BUF', null, null, 330, null],
  ['DAL', 'Dallas Cowboys', 'DEF', 'DAL', null, null, 355, null],
  ['NYJ', 'New York Jets', 'DEF', 'NYJ', null, null, 370, null]
];

const players = {};
for (const [id, name, pos, team, age, exp, rank, injury] of playerRows) {
  const [first, ...rest] = name.split(' ');
  players[id] = {
    player_id: id,
    full_name: pos === 'DEF' ? null : name,
    first_name: pos === 'DEF' ? name.split(' ').slice(0, -1).join(' ') : first,
    last_name: pos === 'DEF' ? name.split(' ').slice(-1)[0] : rest.join(' '),
    position: pos,
    team,
    age,
    years_exp: exp,
    injury_status: injury,
    search_rank: rank,
    fantasy_positions: [pos]
  };
}

// [roster_id, owner_id, wins, losses, fpts, fpts_decimal, players..., starter count]
const rosterDefs = [
  [1, 'u1001', 9, 4, 1687, 45, ['11566', '6768', '9510', '11584', '7547', '11563', '9500', '9484', 'DET']],
  [2, 'u1002', 8, 5, 1543, 72, ['6770', '9998', '3198', '7564', '6801', '9488', '8130', '5857', 'BAL']],
  [3, 'u1003', 7, 6, 1512, 30, ['4984', '5849', '8210', '8151', '6794', '5846', '8144', '9486', 'BUF']],
  [4, 'u1004', 8, 5, 1598, 11, ['7527', '11559', '4866', '12527', '5859', '7525', '11575', '7553', 'PHI']],
  [5, 'u1005', 6, 7, 1450, 90, ['4046', '4988', '4034', '11561', '3163', '8134', '1466', '9481', 'KC']],
  [6, 'u1006', 5, 8, 1389, 25, ['9758', '6797', '6813', '8228', '6819', '11565', '9997', '4217', 'SF']],
  [7, 'u1007', 7, 6, 1533, 60, ['4881', '12526', '9226', '6786', '8137', '2133', '4973', '5844', 'DAL']],
  // orphaned rebuild squad — owner left the league
  [8, null, 2, 11, 1102, 84, ['11560', '8155', '9509', '11562', '8121', '9754', '11596', '3214', 'NYJ']]
];

const rosters = rosterDefs.map(([roster_id, owner_id, wins, losses, fpts, fpts_decimal, ids]) => ({
  roster_id,
  owner_id,
  league_id: LEAGUE_ID,
  players: ids,
  starters: ids.slice(0, 7),
  settings: { wins, losses, ties: 0, fpts, fpts_decimal, waiver_position: roster_id, total_moves: 0 }
}));

const server = http.createServer((req, res) => {
  const send = (body) => {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(body));
  };
  const url = req.url || '';
  console.log(`${req.method} ${url}`);

  if (url === '/v1/players/nfl') return send(players);
  if (url === `/v1/league/${LEAGUE_ID}`) return send(league);
  if (url === `/v1/league/${LEAGUE_ID}/users`) return send(users);
  if (url === `/v1/league/${LEAGUE_ID}/rosters`) return send(rosters);
  // like the real API: unknown league ids return a literal null
  if (url.startsWith('/v1/league/')) return send(null);

  res.writeHead(404).end();
});

server.listen(8080, () => console.log(`sleeper-stub listening on :8080 (league ${LEAGUE_ID})`));
