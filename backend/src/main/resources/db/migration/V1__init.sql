CREATE TABLE league (
    id              TEXT PRIMARY KEY,          -- Sleeper league_id
    name            TEXT        NOT NULL,
    season          TEXT        NOT NULL,
    total_rosters   INT         NOT NULL,
    superflex       BOOLEAN     NOT NULL DEFAULT FALSE,
    last_synced_at  TIMESTAMPTZ
);

CREATE TABLE manager (
    id           TEXT PRIMARY KEY,             -- Sleeper user_id
    league_id    TEXT NOT NULL REFERENCES league(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    team_name    TEXT
);

CREATE TABLE player (
    id             TEXT PRIMARY KEY,           -- Sleeper player_id (or team code for DEF)
    full_name      TEXT NOT NULL,
    position       TEXT,
    nfl_team       TEXT,
    age            INT,
    years_exp      INT,
    injury_status  TEXT,
    search_rank    INT,
    updated_at     TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_player_position ON player(position);

CREATE TABLE roster (
    id                BIGSERIAL PRIMARY KEY,
    league_id         TEXT NOT NULL REFERENCES league(id) ON DELETE CASCADE,
    sleeper_roster_id INT  NOT NULL,
    manager_id        TEXT REFERENCES manager(id),
    wins              INT  NOT NULL DEFAULT 0,
    losses            INT  NOT NULL DEFAULT 0,
    points_for        NUMERIC(8,2) NOT NULL DEFAULT 0,
    UNIQUE (league_id, sleeper_roster_id)
);

CREATE TABLE roster_player (
    roster_id   BIGINT NOT NULL REFERENCES roster(id) ON DELETE CASCADE,
    player_id   TEXT   NOT NULL REFERENCES player(id),
    is_starter  BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (roster_id, player_id)
);

CREATE TABLE value_snapshot (
    id           BIGSERIAL PRIMARY KEY,
    roster_id    BIGINT NOT NULL REFERENCES roster(id) ON DELETE CASCADE,
    captured_on  DATE   NOT NULL,
    total_value  NUMERIC(10,2) NOT NULL,
    qb_value     NUMERIC(10,2) NOT NULL,
    rb_value     NUMERIC(10,2) NOT NULL,
    wr_value     NUMERIC(10,2) NOT NULL,
    te_value     NUMERIC(10,2) NOT NULL,
    avg_age      NUMERIC(4,1),
    UNIQUE (roster_id, captured_on)            -- idempotent daily snapshots
);
CREATE INDEX idx_snapshot_roster_date ON value_snapshot(roster_id, captured_on);

CREATE TABLE sync_log (
    id          BIGSERIAL PRIMARY KEY,
    league_id   TEXT NOT NULL,
    started_at  TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status      TEXT NOT NULL,                 -- RUNNING | SUCCESS | FAILED
    message     TEXT
);
