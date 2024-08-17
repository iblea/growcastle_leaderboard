CREATE TABLE token
(
    bot_name      VARCHAR(128) NOT NULL,
    bot_token     VARCHAR(256) NOT NULL,
    bot_channel   VARCHAR(256) NOT NULL,
    PRIMARY KEY     (bot_name)
);

CREATE TABLE SeasonData
(
    start_date   TIMESTAMP,
    end_date     TIMESTAMP,
    season_name  VARCHAR(16) NOT NULL,
    PRIMARY KEY (start_date, end_date)
);

CREATE TABLE guild_monitor
(
    guild_name   VARCHAR(32) NOT NULL,
    PRIMARY KEY (guild_name)
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


-- update 15 mins (history leaderboard data)
CREATE TABLE History_Player
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    season      VARCHAR(16) NOT NULL,
    min_unit    INT NOT NULL,
    PRIMARY KEY (name, parseTime)
);
CREATE TABLE History_Guild
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    season      VARCHAR(16) NOT NULL,
    min_unit    INT NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE History_Hell
(
    rank        INT NOT NULL,
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    season      VARCHAR(16) NOT NULL,
    min_unit    INT NOT NULL,
    PRIMARY KEY (name, parseTime)
);

-- 길드 멤버
CREATE TABLE guild_member_wave
(
    name    VARCHAR(32) NOT NULL,
    guildname   VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    season      VARCHAR(16) NOT NULL,
    min_unit    INT NOT NULL,
    PRIMARY KEY (name, parseTime)
);





-- CREATE TABLE underdog
-- (
--     name        VARCHAR(32) NOT NULL,
--     score       INT NOT NULL,
--     parseTime   TIMESTAMP NOT NULL,
--     PRIMARY KEY (name, parseTime)
-- );

