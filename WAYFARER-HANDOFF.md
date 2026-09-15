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
- Private eight-member guilds, a shared coin wallet, three house upgrades, furnishings, a guestbook and shared adventure memories.
- Bond milestones: title after one credited clear, Rally after three, twin lantern decoration after five. Credit requires a meaningful encounter and has a five-minute limit; normal rewards still apply to victories.
- Guild Hall and Dungeon Lodge are inside the central capital, reached from the kingdom map.
- Dungeon Lodge presents the existing Rootbound Gate run as a two-player contract, with preparation and rewards expandable. Additional dungeon contracts are not built yet.
- All twelve routes now support shared two-player story missions; both characters must have the route unlocked and be within three levels.

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
7. **Gathering/crafting:** the Blighted Hollow for foraging, the Hollow Vein for mining and the Drowned Archive for fishing. Basic gathering, skill XP, material storage and bundle selling now work. Crafting recipes and deeper profession interactions remain.
8. **Optional later features:** friendly duels, additional settlements, shared housing depth, class combos, world events, cosmetics/emotes and personal surprises.
9. **Endgame after the campaign feels good:** Nightmare/New Game+, another raid boss, rotating challenge dungeon. Tower expansion is not the priority. Casino remains deferred.
10. **Remaining usability:** per-fight basic auto-attack that stops for danger; character deletion with safeguards; mobile/accessibility visual review; deployment confirmation after each release.

## Verification and limits
The Java package build and all 21 tests passed during this pass; frontend JavaScript syntax also passed. Earlier route tests cover saved progress across all twelve routes, gate rejection, first-clear reward limits and early loot ceilings. This is not a long-duration level-1-to-60 balance study, full browser/device accessibility audit, or confirmation of live deployment. Avoid repeatedly running broad simulations; use player feedback and focused checks as changes warrant.

Detailed plot and future scene design: STORY-PATH.md. Original requirements and checkpoint history: DEVELOPMENT-PLAN.md. This handoff supersedes older statements that the twelve sub-areas or guild housing were unbuilt.

### Cinder palette follow-up
The interface now uses crimson/ember buttons, health and resource bars, selected navigation, class selection, promotion highlights and warm ash surfaces in both themes. The app icon is a flame. Map hover anchoring is retained. Natural terrain and equipment rarity colors remain distinguishable.

## Shared story, capital and professions release

- Central Cindergard citadel opens the Crown District. Guild Hall, Dungeon Lodge, inn, forge and apothecary are inside. Combat regions surround the capital; foraging woods, mining grounds and a fishing island are separate map destinations. This supersedes the earlier map layout.
- Twelve shared story contracts. Each has three combat stages, regroup recovery, route-specific narrative and one of three objectives: escort supplies, recover evidence or disrupt wards. Objectives consume a player's turn, creating support/attack decisions. A guardian kill alone cannot finish a stage.
- Escort actions restore supply integrity; neglect can fail the mission. Investigation actions expose the searching player to extra damage, encouraging partner protection. Ward disruption reduces incoming enemy damage that round.
- Story route must be open to both accounts; a further-progressed character can help on an earlier route. The existing three-level party range remains. Each character receives its own saved route clear and first-clear XP only once. Final routes grant regional clears/promotions. Repeats give ordinary run rewards, never duplicate story bonuses. Leaving/failure gives no story clear.
- Pending choices, stages, objective progress and supply integrity survive restart in the existing co-op save payload. Rescue, Rally, guild supplies and pair-based bonds remain supported. The old Warden dungeon remains available.
- Guild capacity is eight accounts. Each member can select a guildmate to view that pair's bond; Rally eligibility is still determined by the actual two-person party. Invites rotate after joins. The founder still cannot leave until other members leave; ownership transfer/moderation are not implemented.
- Foraging, mining and fishing: three sites each, ten profession levels, 25 XP per gathering trip, 8 resource cost, per-character materials and five-item sale bundles. Better sites require both skill level and story access. Resource recovery remains at the inn. No crafting yet.
- Removed grey navigation-tab backgrounds in both themes. Existing map-hover fixes remain.
- Verification: full package build and 28 Java tests; isolated two-player shared-story run with reconnect/restart and duplicate reward denial; isolated eight-member guild test with ninth-member rejection. Map SVG and frontend syntax checked. No long-term economy study or full device visual audit.

### Later world expansion (requested, deliberately not built now)
Branch into other kingdoms with their own goals, wars, monster threats and local crises. Cindergard should first feel complete and enjoyable. Future kingdom access should follow authored story milestones, preserve existing characters/professions/guilds and offer new reasons to cooperate. No war system or second kingdom is included in this release.

### Remaining priority
Solo mission-objective parity, richer investigation/escort interactions, class builds and promotion mechanics, legendary/crafting goals, profession-specific minigames, more visual variety and targeted balance from player feedback. Larger-party raids, guild ownership transfer and rival duels remain unbuilt.

## Solo objectives, crafting and death-penalty checkpoint

- New solo expeditions use the shared mission themes. Each combat site requires two objective actions. Escort actions restore supply integrity; ignoring the convoy can fail the expedition. Ward disruption halves damage for that turn. Searches take the player's turn without attacking. If enemies are defeated first, remaining site work is completed before progression. Saved progress survives reload. Legacy active expeditions without mission flags can finish normally.
- Iven's forge now offers four deterministic recipes: reinforced trail coat, Piercing ironwood class weapon, Focus wardbound class weapon and two field potions. Recipes show costs/results and require character level plus the appropriate story route. Materials, coins and capacity are checked before spending. Legendary equipment is still unbuilt.
- Death now costs 15/20/25/30 percent of current-level XP, 10/15/20/25 percent of coins and 5/10/15/20 percent of each gathered material stack, according to the highest opened region. XP/coin losses round up; material losses round down. No deleveling or equipment destruction. Exact losses are recorded in Recent events.
- Co-op applies that penalty only to characters still fallen when the run ends. A successfully rescued character does not receive it. A living character who loses the convoy does not receive a death penalty. Failed missions still award no story clear.
- Full build and 32 tests passed, including objective turn cost/persistence, convoy failure without progression, atomic crafting, no free repeat crafting and individual co-op death losses. No repeated long-duration simulations were run.
- Remaining: richer investigation choices, more crafting recipes, subclass/attribute/loadout choices, legendary goals, varied enemy art, guild leadership tools and long-term economy tuning. Future kingdoms remain deferred.

## Small combat and guild polish release
- Solo enemy warnings now include practical response advice. Turn summaries show net enemy health, player health and resource changes.
- Frost Elementals periodically siphon eight resource; guard blocks it. Dark Cultists periodically heal instead of attacking; stun interrupts that recovery. Shared-story enemy patterns remain their existing co-op implementation.
- Leaders can transfer ownership to a current guildmate through a confirmed form. Server checks ownership and membership; the invite rotates on transfer. The former leader can then leave.
- Fixed a brittle deployment test that compared JSON set ordering; it now compares complete saved-state values instead.
- Full build and 35 tests passed. Attribute allocation, ability leveling, loadouts, subclass mechanics and legendary rewards remain next larger systems.

### Lightweight equipment-art update
Seven custom SVG silhouettes replace generic sword/shield artwork: sword, staff, mace/sceptre, paired daggers, coat, mail and plate. Item names select weapon silhouettes with class fallback for older generic names. Equipped slots, backpack, item details, chest reveals and co-op reward cards share the same 2,992-byte SVG sheet. Rarity accents work in both themes. JavaScript syntax and SVG structure checked; game mechanics unchanged.

## Character builds release

Implemented two attribute points per level after level one (Power +1 attack, Vitality +4 max health, Armour +1 defence, Focus +2 max resource; 60-point cap each), one training point every three levels, three upgrade ranks per ability, four-slot configurable loadouts, eight specialisations and eight promotion techniques. The original four abilities remain the default loadout for existing characters. First regional clear opens specialisation and a signature technique; Caves clear opens the second new technique.

Ability ranks reduce paid costs by one per rank, add 8% direct damage/healing per rank and +2 recovery per rank for resource-restoring abilities. Status durations are not increased. Free build reset and specialisation changes are capital-only; normal co-op locking prevents mid-party changes. Increased capacity is not a free heal. Saves and co-op player reconstruction use the same build configuration.

Specialisations: Warrior Vanguard (+15% armour/+10% health) or Champion (+12% ability damage); Mage Battle Mage (+12% ability damage) or Spellwarden (+15% armour/+20 resource); Cleric Lightwarden (+20% direct healing) or Inquisitor (+12% ability damage); Rogue Assassin (+12% ability damage) or Pathfinder (+10% health/+20 resource). See the in-game Class & abilities screen for requirements and exact technique descriptions.

Gear: Conduit restores 3 resource after a damaging ability; Vampiric restores 3 health after a damaging basic attack. Added Conduit and Bloodsteel recipes; later drops can roll the new attributes. Existing Focus/Piercing/Siphon items remain valid. No legendary rarity added.

Six mixed-class co-op combinations: Oathkeepers, Dawnfire, Hidden Mercy, Shielded Inferno, Flanking Guard, Smoke and Cinders. Coordinate two abilities or protection plus an ability, with actual damage dealt. Three-round persisted cooldown. Cleric pairs heal, non-Cleric Warrior pairs guard, Mage/Rogue restore resource; all add a small shared strike.

Validation: full Maven package and 41 tests passed; JavaScript syntax passed. The isolated HTTP check now also passed: earned attributes, saved loadout, reconnect persistence, premature specialisation rejection, shared-story restart and duplicate reward rejection. Saved for the rework branch; push and confirm the Render deployment before treating this release as live.
