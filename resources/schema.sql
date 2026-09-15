CREATE SCHEMA IF NOT EXISTS hearthglen;
CREATE TABLE IF NOT EXISTS hearthglen.rpg_users (
    username VARCHAR(32) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_saves (
    username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username),
    version BIGINT NOT NULL,
    payload TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_scores (
    username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username),
    character_name VARCHAR(40) NOT NULL,
    player_class VARCHAR(16) NOT NULL,
    level INTEGER NOT NULL,
    kills INTEGER NOT NULL,
    bosses INTEGER NOT NULL,
    tower INTEGER NOT NULL,
    ranked BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_developer_saves (
    username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username),
    version BIGINT NOT NULL,
    payload TEXT NOT NULL
);
