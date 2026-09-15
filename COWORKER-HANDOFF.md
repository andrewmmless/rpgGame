# Latest update

The social release adds private two-player guilds, a shared house with three upgrades, decorations, pooled contributions, trophies, bond milestones/Rally, memories and preset reactions. Region-specific drops now cap early equipment and reduce epic availability. See SOCIAL-RELEASE.md for the current implementation and release requirements; SHARE-SUMMARY.md is the short summary.

The user reduced scope due to 30% remaining usage. Friendly PvP, a second town, multi-room adventure, expanded combat visuals/combos, world events and legendary armour remain pending. Older status below is historical and superseded by the social release.

# Wayfarer — development handoff

Prepared September 15, 2026. This is a small Java/Spring Boot RPG intended primarily for two players. The goal is meaningful cooperation and short enjoyable sessions, rather than repetitive attack clicking.

## Completed

- Four classes, four campaign regions, turn-based abilities/status effects, area bosses and an Endless Tower.
- Illustrated clickable town with an inn, equipment shop, potion shop and quest noticeboard.
- Equipment loadout, comparisons, sorting, upgrades, individual and confirmed bulk sales.
- Server-enforced item protection: protected and equipped gear cannot be sold; bulk selections are validated before anything changes.
- Class-specific names on newly acquired weapons and lower resale prices. Existing gear retains its names and combat stats.
- Earned milestone chests with animated keep/sell reveals and server-saved rewards.
- Two-player Rootbound Gate boss encounter with invite codes, shared rounds, protect/mend, class abilities, persistent pending turns, and individually awarded loot.
- Six-character case-insensitive party codes with copy support and legacy 16-character compatibility; join attempts are limited.
- Explicit turn/waiting indicators and persisted per-player reward summaries, including full-bag coin conversions.
- Partner rescue: the first single-player knockdown pauses combat for up to 60 seconds. The survivor can restore the partner to 35% health, once per run. A missed rescue, simultaneous defeat or a later knockdown ends the run. Timed-out rescues are finalized when a player returns/acts; no background timer is required.
- Clearer dungeon completion/failure messages and round/rescue totals on victory summaries.
- Accounts, database autosaves, leaderboard protection, disabled browser save imports, and an optional isolated owner-only Developer Lab.

## Still to do

1. Four character slots per account, with a safe migration of existing saves and character-bound party membership.
2. Expanded context-sensitive death penalties; currently the existing penalties remain. No new XP or inventory-loss system was introduced here.
3. Combo attacks, attribute allocation, ability upgrades/loadouts, distinctive legendary items and rare-reward progression.
4. Nightmare campaign, a second mechanically different co-op dungeon and rotating challenges.
5. Narrative/rival content and exploration variety.
6. Optional later gathering/crafting and friendly duels; housing, guilds and subclasses remain deferred.

The co-op release is one boss encounter, not a complete multi-room dungeon system. Gameplay balance and enjoyment still need short human playtests.

## Verification and limits

Prior usability batch: focused bulk-sale/pricing checks and a two-account co-op run covering short codes, simultaneous moves, restart with a pending action, reward summary and duplicate reward rejection.

Current batch: focused rescue tests (knockdown, serialization, survivor-only rescue, one-use restriction and expiry), plus protected individual/bulk sales and save preservation. JavaScript syntax and Java build checked. No large balance simulation or repeated full-suite run; this follows the user's request to conserve usage. The new UI has not had a full cross-device visual pass.

## Release steps

Code is committed on rework. Main is unchanged. Push origin from GitHub Desktop when ready, then wait for the Render deployment to finish. Local commits and GitHub pushes alone do not prove a release is live. Verify the displayed short invite, co-op summary, item-protection control and rescue flow on the deployed build.

No database-table migration is needed for this batch. New rescue/reward fields are stored in existing party JSON; item protection uses existing persisted character metadata. Older parties default to zero rescues used. Existing completed parties without reward summaries retain their battle logs.

The owner Developer Lab requires DEVELOPER_USERNAME=andrew in Render, for the existing account. This handoff does not confirm that setting has been enabled.

## Hosting and data

GitHub stores source code. Render runs the web service. Supabase stores live accounts and game saves. Local preview databases are separate. Credentials are not included in this document and should not be uploaded or shared with the handoff.

Before the future character-slot migration, establish a recoverable Supabase backup and verify rollback. No hosting-plan upgrade is needed merely to ship this batch; no paid-plan change was made.

See ROADMAP.md for the active idea list.
