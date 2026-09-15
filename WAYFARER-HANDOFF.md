# Wayfarer — current build and remaining work

## Product direction
A small Java RPG for Andrew and his girlfriend. The player serves the kingdom of Cindergard: protects roads and settlements, earns standing through service, and uncovers a broken pact. Noble-era adventure, a recurring company and personal rivalry provide the tone. Story victories open new routes and equipment ceilings. Players can hunt freely within unlocked content; there is no required daily schedule. The Tower is optional, and casino work is deferred.

This is the source-code handoff, not confirmation of a successful live Render deployment. Push the rework branch and check Render's deploy result. Main remains untouched.

## Completed: story and world
- Wayfarer is the game title; Cindergard is the kingdom; Hearthglen is a settlement offering services.
- Twelve playable solo routes use the existing encounter/supply-stop/guardian structure. Each has an NPC mission introduction, named guardian and first-clear story conclusion.
- Woods, levels 1–15: Old Timberline, Hollow Reach, Blighted Grove.
- Caves, levels 16–30: Entrance Tunnels, Deep Shaft, The Fracture.
- Ruins, levels 31–45: Outer Colonnade, Sunken Hall, The Drowned Archive.
- Spire, levels 46–60: Ashen Approach, The Scarred Path, The Wyrm's Hollow.
- Routes unlock sequentially through clears, not character level. Level ranges recommend preparation; guardians sit at the route's upper level.
- Open routes stay repeatable. First clears give bonus XP once; subsequent patrols give normal fight rewards.
- Story: supply shortages, deliberately stripped royal wards, a noble requisition, pact evidence, Cedric choosing service over his house, the dragon confrontation and court resolution.
- Recurring cast: Captain Elin Ward, courier Tessa Reed, noble recruit Cedric Ashford, Keeper Rowan Flint, Mara the innkeeper and Iven the blacksmith.
- The dragon is now **Ashwing, the Red Wyrm**, replacing Victoria in current authored content.
- Existing completed regions remain completed and unlock their routes; old active expeditions can finish. Story scenes are available in the journal archive.

## Completed: classes and combat
- Warrior, Mage, Cleric and Rogue are chosen at character creation. Players do not have to unlock the base classes in the story.
- Four abilities per class, unlocked at levels 1, 2, 5 and 8. Attacks, abilities, defend, potions, escape, cooldowns and reusable status effects operate in the Java engine.
- Promotions are derived from regional victories, with no level shortcut. They are currently recognition/title progression, not new subclass mechanics.
- Warrior: Squire → Knight → Knight-Lieutenant → Knight-Captain → Knight Commander.
- Mage: Apprentice → Battle Mage → Court Mage → Royal Magus → Archmagus.
- Cleric: Acolyte → Field Healer → Court Healer → High Healer → Grand Healer.
- Rogue: Scout → Ranger → Royal Scout → Spymaster → Shadow Warden.
- Dragonbane is separate from rank and requires the final region clear.
- Class & abilities now explains class selection, shows the full promotion path and identifies each required regional boss.
- Level-ups add only new health/resource capacity, not a full heal. XP requirements grow more slowly than before the rebalance in terms of levels gained per battle.
- Heavy attacks are telegraphed and stronger; defending halves incoming damage and grants extra resource recovery.

## Completed: equipment and economy
- Weapon and armour slots; comparison, sorting, equipping, upgrading and selling.
- Item protection and atomic bulk selling exclude protected/equipped items. Common and rare selection buttons; sale review before confirmation.
- Bulk panel expansion and page scroll are preserved on redraw.
- Piercing adds damage, Siphon heals from damaging abilities, Focus adds resource when defending.
- Regional names and route-level ceilings prevent high-level characters farming top-tier items in entry routes. Shop and chest ceilings follow opened routes.
- Epic drops start at item level 31. Boss/chest epic chance is 8%; ordinary epic chance is 2% when eligible. Existing items are retained.
- Milestone chests have a reveal animation and Keep/Sell choice. Full inventory handling is supported.
- Inn recovery, potion shop, forge purchases and upgrades, introductory training and existing journal rewards remain.
- Current solo death penalty is 10% of coins. The document's XP/material loss system is not implemented.

## Completed: co-op and social systems
- One two-player Rootbound Gate dungeon, coordinated turns, six-character invite codes, a pending-turn indicator, partner protection/healing, downed-partner rescue, and individual reward summaries.
- Normal solo actions are locked while a party is active. Characters cannot switch slots while still in a party.
- Private two-member guilds, a shared coin wallet, three house upgrades, furnishings, a guestbook and shared adventure memories.
- Bond milestones: title after one credited clear, Rally after three, twin lantern decoration after five. Credit requires a meaningful encounter and has a five-minute limit; normal rewards still apply to victories.
- Guild Hall and Dungeon Lodge are now buildings on the kingdom map instead of permanent navigation tabs.
- Dungeon Lodge presents the existing Rootbound Gate run as a two-player contract, with preparation and rewards expandable. Additional dungeon contracts are not built yet.
- Shared chapter progression is not implemented; the twelve-route campaign is currently solo.

## Completed: presentation in this pass
- New illustrated opening screen: crown-service framing, fortress landscape and red/ember palette.
- Red dragon artwork for Ashwing, restrained breathing/ember animation, reduced-motion support.
- Light and dark modes; preference saved in this browser, with system preference used initially. This is device-local, not an account-wide setting.
- Kingdom map shows four regions; the selected region focuses on the next destination. Earlier hunts and unopened routes are tucked into a disclosure.
- Story dialogue/history stays expandable rather than filling the map with text.
- Guild Hall and Dungeon Lodge have building-shaped map markers and a return-to-map control.
- Fixed marker hover jump: hover now preserves the positioning transform.
- Backup button removed from the UI. The export endpoint still exists for support; save import remains disabled.
- Class creation and promotion explanations are clearer.

## Accounts, saves and deployment
- Four character slots per login, one per base class; independent gold, equipment and progression.
- Existing character occupies the first slot. Guild/bond membership and developer eligibility remain account-wide.
- Active character drives the current account leaderboard entry. Inactive characters are not separately ranked.
- Autosaves are server-side. Active saves: hearthglen.rpg_saves; inactive slots: rpg_character_slots; slot selection: rpg_slot_state. Accounts: rpg_users; leaderboard: rpg_scores. Guild, bond and co-op records have separate tables.
- Live configuration uses Supabase PostgreSQL through Render. Local development uses an isolated H2 database unless configured otherwise. Do not commit passwords or connection secrets.
- Developer Lab is a separate unranked sandbox enabled through DEVELOPER_USERNAME. Character deletion UI is still absent.
- Optimistic versions and character generation checks reject stale mutations. Rewards remain server-controlled; save imports are blocked.
- Render's previous failure was an obsolete test expectation after the defend change. That was corrected; deployment tests remain enabled.

## What still needs doing, in order
1. **Mission mechanics and atmosphere:** actual convoy defense, evacuation, watchpost investigation, attack events and environmental decisions. Current story describes these duties, but route gameplay still uses standard encounters. Add varied enemy silhouettes, hit feedback, sound controls and route-specific scenery where useful. Do not substitute animations for decisions.
2. **Co-op campaign:** shared mission lobbies, individual eligibility, progress for players on different chapters, repeat-help rewards and no duplicate story rewards. Add dungeon contracts and branching/multi-room runs afterward.
3. **Progression choices:** attributes and points, ability levels/loadouts, real subclass choices and mechanical promotion rewards. Current rank names alone do not provide these systems.
4. **Long-term gear goals:** legendary items, distinctive boss rewards, crafting/pity progress, more build-defining attributes. Avoid repetitive ten-second farming loops.
5. **Story depth:** interactive conversations/choices, playable rival contests, promotion ceremonies, branching consequences and richer NPC reactions. Keep story quests with NPCs; noticeboards should only hold optional small tasks.
6. **Death/economy refinements:** story-tier-scaled XP and unequipped-material risk; never lose levels or equipped gear; penalize only the fallen co-op player; exempt rival contests. Review income, item values and difficulty using real play feedback.
7. **Gathering/crafting:** the Blighted Hollow for foraging, the Hollow Vein for mining and the Drowned Archive for fishing. These are planned names, not functioning professions. Add skills, resources and recipes.
8. **Optional later features:** friendly duels, additional settlements, shared housing depth, class combos, world events, cosmetics/emotes and personal surprises.
9. **Endgame after the campaign feels good:** Nightmare/New Game+, another raid boss, rotating challenge dungeon. Tower expansion is not the priority. Casino remains deferred.
10. **Remaining usability:** per-fight basic auto-attack that stops for danger; character deletion with safeguards; mobile/accessibility visual review; deployment confirmation after each release.

## Verification and limits
The Java package build and all 21 tests passed during this pass; frontend JavaScript syntax also passed. Earlier route tests cover saved progress across all twelve routes, gate rejection, first-clear reward limits and early loot ceilings. This is not a long-duration level-1-to-60 balance study, full browser/device accessibility audit, or confirmation of live deployment. Avoid repeatedly running broad simulations; use player feedback and focused checks as changes warrant.

Detailed plot and future scene design: STORY-PATH.md. Original requirements and checkpoint history: DEVELOPMENT-PLAN.md. This handoff supersedes older statements that the twelve sub-areas or guild housing were unbuilt.
