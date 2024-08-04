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


CREATE TABLE `underdog`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `underdog`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `sayonara`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `redbridge`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `paragonia`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `droplet`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `777`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `skeleton_skl`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

CREATE TABLE `shalom`
(
    name        VARCHAR(32) NOT NULL,
    score       INT NOT NULL,
    parseTime   TIMESTAMP NOT NULL,
    PRIMARY KEY (name, parseTime)
);

-- CREATE TABLE `under dog`
-- (
--     name        VARCHAR(32) NOT NULL,
--     score       INT NOT NULL,
--     parseTime   TIMESTAMP NOT NULL,
--     PRIMARY KEY (name, parseTime)
-- );


delete from `Leaderboard_Player`;
delete from `Leaderboard_Guild`;
delete from `Leaderboard_Hell`;
delete from `underdog`;
delete from `sayonara`;
delete from `redbridge`;
delete from `paragonia`;
delete from `droplet`;
delete from `777`;
delete from `skeleton_skl`;
delete from `shalom`;



show tables;
