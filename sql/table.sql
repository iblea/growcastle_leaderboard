CREATE TABLE token
(
    bot_name      VARCHAR(128) NOT NULL,
    bot_token     VARCHAR(256) NOT NULL,
    bot_channel   VARCHAR(256) NOT NULL,
    PRIMARY KEY     (token_name)
);

-- drop table Leaderboard_Player;
-- show columns from LEADERBOARD_PLAYER;

CREATE TABLE Leaderboard_Player
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE Leaderboard_Guild
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE Leaderboard_Hell
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);
