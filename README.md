# Hearthglen — Andrew's RPG

A playable Java browser RPG, built on the V4 foundation. Four classes, four regions, five-stop expeditions, boss encounters, equipment, quests, accounts, and database-backed autosaves.

## Play locally

Requires Java 17+ (Java 21 recommended). Open `pom.xml` in IntelliJ as a Maven project, or double-click `start-web.command` on macOS. The first build downloads dependencies. Visit http://localhost:18080 and create an account.

To rebuild after editing:

```
./mvnw package
java -jar target/rpg-game-4.1.0.jar --server.port=18080
```

New players can import an existing console `.txt` save during character creation. Original save files remain unchanged. Run `Main` for the original console game; it shares the player and combat foundation, but the new campaign is played in the browser.

## What is playable

- Warrior, Mage, Cleric, Rogue: four abilities each, unlocking at levels 1, 2, 5, and 8.
- Hearthglen town: free rest, potion shop, introductory training.
- Whispering Woods, Stonefang Caves, Forgotten Ruins, Dragon's Spire: three encounters, a spring/cache choice, and an area boss per expedition.
- Later regions require the previous boss clear and a minimum level. Repeat trails to gain levels.
- Bosses telegraph heavy attacks. Later enemies can guard, poison, drain health, or charge heavy attacks. The opening Woods are intentionally forgiving.
- Equipment drops with three rarities, two slots, selling, and five upgrades per item. Bosses guarantee rare/epic equipment.
- Five reward quests, four milestones, and an Endless Tower after defeating Victoria.
- Defeat loses 10% of coins and ends the expedition; recovery keeps character and equipment.
- Accounts, sign-in/out, autosave after each accepted action, mid-combat resume, console-save import, downloadable JSON backup.

This is a first playable campaign, not a finished commercial release. There is no multiplayer, email/password recovery, multiple character slots, branching dialogue, or complex crafting. Expeditions use a fixed five-stop structure with randomized opponents and loot. Artwork is stylized and shared between regions; individual enemy illustrations remain future polish.

## Architecture and saves

Core Java files stay in `src/`; no Spring imports in the game rules. `GameSession` controls the campaign, `CombatEngine` controls one encounter, and `GameSave` is a versioned data-only snapshot. `src/web/` holds the Spring Boot adapter with explicitly imported components, preserving the original default-package console layout. A future package migration should update all classes together.

The browser sends actions, never authoritative health, damage, or rewards. PostgreSQL/H2 store account hashes and game JSON in the private `hearthglen` schema. Passwords use BCrypt, mutations require CSRF tokens, account identity comes from the authenticated session, and optimistic version checks reject stale tabs. Accepted commands and their snapshots commit together. Re-sign-in after a server restart is expected; the saved game resumes unchanged.

Local saves are in ignored `data/`. Back up that directory only while the local server is stopped. Production uses external PostgreSQL. Save schema version is 1; future changes need a migration before increasing it. Downloaded JSON backups currently require an administrator-assisted restore; the in-game import accepts console .txt saves only. Console files remain in their original locations and are not uploaded until the player chooses a file.

## Verification

```
./mvnw verify
sh test.sh
python3 tests/web_smoke.py
```

Campaign tests cover a full four-region progression with serialization between actions, mode/area gates, equipment scaling, boss telegraphs, all ability unlocks, and 400 starter expeditions. The original 84 checks include 4,000 normal-fight balance simulations. HTTP tests start an isolated local server and check authentication, CSRF, separate accounts, stale versions, imports, and saves surviving a server restart. They require Python 3 and permission to bind a local port.

## Deploy

See [DEPLOYMENT.md](DEPLOYMENT.md). The Dockerfile and Render blueprint are included; production account/database setup is still required. No public deployment or paid service has been created.
