# Wayfarer

A playable Java browser RPG, built on the V4 foundation. Four classes, four regions, five-stop expeditions, boss encounters, equipment, quests, accounts, and database-backed autosaves.

## Play locally

Requires Java 17+ (Java 21 recommended). Open `pom.xml` in IntelliJ as a Maven project, or double-click `start-web.command` on macOS. The first build downloads dependencies. Visit http://localhost:18080 and create an account.

To rebuild after editing:

```
./mvnw package
java -jar target/rpg-game-4.1.0.jar --server.port=18080
```

Web save imports are disabled. Original console save files remain unchanged. Run `Main` for the original console game; it shares the player and combat foundation, but the new campaign is played in the browser.

## What is playable

- Warrior, Mage, Cleric, Rogue: four abilities each, unlocking at levels 1, 2, 5, and 8.
- Hearthglen town: free rest, potion shop, introductory training.
- Whispering Woods, Stonefang Caves, Forgotten Ruins, Dragon's Spire: three encounters, a spring/cache choice, and an area boss per expedition.
- Later regions require the previous boss clear and a minimum level. Repeat trails to gain levels.
- Bosses telegraph heavy attacks. Later enemies can guard, poison, drain health, or charge heavy attacks. The opening Woods are intentionally forgiving.
- Equipment drops with three rarities, two slots, selling, and five upgrades per item. Bosses guarantee rare/epic equipment.
- Five reward quests, four milestones, and an Endless Tower after defeating Victoria.
- Defeat loses 10% of coins and ends the expedition; recovery keeps character and equipment.
- Accounts, sign-in/out, autosave after each accepted action, mid-combat resume, earned chest reveals, downloadable JSON backup.

This is a first playable campaign, not a finished commercial release. There is no multiplayer, email/password recovery, multiple character slots, branching dialogue, or complex crafting. Expeditions use a fixed five-stop structure with randomized opponents and loot. Artwork is stylized and shared between regions; individual enemy illustrations remain future polish.

## Architecture and saves

Core Java files stay in `src/`; no Spring imports in the game rules. `GameSession` controls the campaign, `CombatEngine` controls one encounter, and `GameSave` is a versioned data-only snapshot. `src/web/` holds the Spring Boot adapter with explicitly imported components, preserving the original default-package console layout. A future package migration should update all classes together.

The browser sends actions, never authoritative health, damage, or rewards. PostgreSQL/H2 store account hashes and game JSON in the private `hearthglen` schema. Passwords use BCrypt, mutations require CSRF tokens, account identity comes from the authenticated session, and optimistic version checks reject stale tabs. Accepted commands and their snapshots commit together. Re-sign-in after a server restart is expected; the saved game resumes unchanged.

Local saves are in ignored `data/`. Back up that directory only while the local server is stopped. Production uses external PostgreSQL. Save schema version is 1; future changes need a migration before increasing it. Downloaded JSON backups currently require an administrator-assisted restore. Web save imports are disabled. Console files remain in their original locations.

## Verification

```
./mvnw verify
sh test.sh
python3 tests/web_smoke.py
```

Campaign tests cover a full four-region progression with serialization between actions, mode/area gates, equipment scaling, boss telegraphs, all ability unlocks, and 400 starter expeditions. The original 84 checks include 4,000 normal-fight balance simulations. HTTP tests start an isolated local server and check authentication, CSRF, separate accounts, stale versions, blocked imports, developer access and isolation, chest sales, and saves surviving a server restart. They require Python 3 and permission to bind a local port.

## Deploy

See [DEPLOYMENT.md](DEPLOYMENT.md). The Dockerfile and Render blueprint are included; production account/database setup is still required. No public deployment or paid service has been created.

The current prototype adds an illustrated clickable town, a blacksmith shop, an equipment comparison interface, milestone chests with keep/sell confirmation, and functional weapon attributes. The owner can enable a separate unranked Developer Lab through `DEVELOPER_USERNAME`; see DEPLOYMENT.md. The proposed co-op, profession, subclass, housing and endgame redesign is recorded in COOP-DESIGN.md and is not implemented yet.

## Two-player co-op
Open Co-op with a normal character in town. One player creates a party and shares the six-character code; the other joins. Characters must be within three levels. The host starts the Rootbound Gate encounter. Both players lock a move each round; attacks, class abilities, defend, protect, mend and up to three carried potions are available. A missing player can default to defend after one minute if the other has chosen. Either player can leave to end the run. Solo actions pause during a lobby or battle.

This first co-op release is one shared boss encounter, not a full branching dungeon. Victory automatically saves separate XP, coins and a rare-or-epic item for each participant. Full bags convert loot to coins. Sessions and pending moves survive refresh/server restart; completion and rewards share one database transaction. Developer test characters cannot enter. `python3 tests/coop_smoke.py` runs one isolated two-account victory/reconnect/replay check. Broader balance playtesting remains necessary.

### Usability update
New parties use six-character, case-insensitive invite codes with a copy button; existing 16-character codes remain valid. Join attempts are rate-limited. Co-op has an explicit waiting-for indicator and a persisted per-player reward summary (including full-bag coin conversion). Older completed runs retain their battle logs.

Equipment now supports selecting multiple unequipped items for a reviewed, atomic bulk sale. All selected IDs must be valid and unequipped or nothing is sold. Resale is `2 + item level + 4 × rarity tier + 2 × upgrades` coins (tiers: common 0, rare 1, epic 2); new prices apply to existing gear too. Newly acquired weapons use class-specific names without changing stats; existing item names remain unchanged.

### Partner rescue and item protection
One single-player knockdown per co-op run opens a 60-second rescue window. The survivor can revive their partner at 35% health. A later knockdown, both players falling, or an expired rescue ends the run. Rescue pauses are persisted and keep the normal character locked to the party. Mark equipment as protected to exclude it from individual and bulk selling; unprotect it explicitly to sell.

See COWORKER-HANDOFF.md for release steps and verification limits, and ROADMAP.md for the active development plan.

## Social release
Guilds, shared housing/furnishings, bond milestones/Rally, shared memories and region-tier loot are now implemented. See SOCIAL-RELEASE.md for exact limits, changed drop rates, tests and pending features. SHARE-SUMMARY.md is the short shareable status.
