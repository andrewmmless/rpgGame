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

CREATE TABLE IF NOT EXISTS hearthglen.rpg_coop (
    id VARCHAR(32) PRIMARY KEY,
    active BOOLEAN NOT NULL,
    payload TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_coop_members (
    username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username),
    party_id VARCHAR(32) NOT NULL REFERENCES hearthglen.rpg_coop(id)
);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_social_lock (id INTEGER PRIMARY KEY);
INSERT INTO hearthglen.rpg_social_lock(id) SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM hearthglen.rpg_social_lock WHERE id=1);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_guild (id VARCHAR(32) PRIMARY KEY, payload TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_guild_member (username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username), guild_id VARCHAR(32) NOT NULL REFERENCES hearthglen.rpg_guild(id));
CREATE TABLE IF NOT EXISTS hearthglen.rpg_bond (id VARCHAR(70) PRIMARY KEY, payload TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_slot_state (username VARCHAR(32) PRIMARY KEY REFERENCES hearthglen.rpg_users(username), active_slot INTEGER NOT NULL, generation BIGINT NOT NULL);
CREATE TABLE IF NOT EXISTS hearthglen.rpg_character_slots (username VARCHAR(32) NOT NULL REFERENCES hearthglen.rpg_users(username), slot INTEGER NOT NULL, payload TEXT NOT NULL, ranked BOOLEAN NOT NULL, PRIMARY KEY(username,slot));
