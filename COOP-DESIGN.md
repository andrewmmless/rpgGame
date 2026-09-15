# Wayfarer: a game for two

Design proposal, not a list of implemented features. Andrew wants a game he and his girlfriend want to return to, inspired by the variety, professions, crafting, discoveries and dungeon unlocks described in the EPIC RPG wiki he supplied. The existing attack/next/level loop is too easy and repetitive.

## The session we are building toward

Spend 10–20 minutes gathering or crafting something useful, meet in a town, choose a dungeon route together, coordinate a few consequential combat turns, discover a reward, and make progress on a shared home. Both solo preparation and playing together should advance shared goals. Avoid mandatory daily chores, long artificial waits and rewards for sheer click volume.

## First complete slice

One illustrated town with an innkeeper, a blacksmith and an herbalist; a proper shop; a short connected NPC quest; three gathering/crafting activities; one dungeon playable solo or by two people; and a shared cottage project. Build and playtest that whole loop before adding many towns or a full guild system.

Combat shows both characters and recognizable enemies with distinct art, readable intentions, impact feedback, status icons, and short skippable effects. Party members choose actions, then a round resolves on the server. Include reconnection, turn deadlines with safe auto-defend, and withdrawal rules so a disconnect cannot trap the other player. No paid infrastructure upgrade is assumed; measure memory and traffic before changing the hosting plan.

## Progression and choices

Keep Warrior, Mage, Cleric and Rogue. Introduce two specializations per class after an introductory quest, with inexpensive respecialization so a pair can experiment. Proposed examples: Guardian/Berserker, Frostweaver/Pyromancer, Warden/Inquisitor, Duelist/Trickster. These are proposed names and mechanics, not implemented classes.

Specializations alter actions and team combinations, rather than multiply every stat. Example: one player marks an enemy weakness, the other exploits it; one protects a partner while that partner prepares a longer spell. Every class must have a viable solo route, and no pair should require a particular healer class.

Character level remains a modest progression summary. Skills unlock practical capabilities:
- Combat: weapon techniques and subclass choices.
- Foraging: ingredients, herb identification and gathering choices.
- Crafting: recipes, equipment modification and efficient material use.
- Dungeoneering: scouting, traps and alternate paths.

An encounter can award combat XP and a smaller dungeoneering award, each once on completion. Gathering advances foraging, rather than granting unrestricted strength. Cap passive combat bonuses from noncombat skills; favor new options and efficiency. No reward for rejected requests, repeated room entry, hitting an invulnerable target or repeatedly claiming an objective.

## Difficulty and endgame

Replace automatic enemy scaling that follows every level with authored difficulty bands and optional harder routes. Early encounters teach defend, interrupts and combinations; later encounters test them. Normal attacks should not solve every mechanic. Avoid simply adding health, unavoidable damage, or enemies that invalidate a whole class.

Use limited expedition preparation, capped consumables carried into a dungeon, branching rooms and optional elite encounters to create risk/reward decisions. Healing and resource economies must be tested across every class and pair before changing current saves. Failure should cost the current run's bonus rewards without deleting core equipment or undoing hours of progress.

Endgame: dungeon modifiers, mechanically different bosses, optional challenge objectives, build-changing equipment, crafting goals and house trophies. Cooperative rewards should compensate both participants fairly; give individual loot plus shared project materials. Never make the stronger partner take the weaker partner's rewards. Guild halls can extend the shared-home system after two-player ownership and contributions work reliably.

## Equipment and earned chests: implemented prototype

Equipment now has two loadout slots, a filtered/sorted backpack, one selected-item comparison and focused equip/upgrade/sell controls. Sales require an in-game confirmation. The prototype adds a milestone chest after the first three wins, each first region clear and each new tower floor. Chests grant one server-rolled item, 70% rare and 30% epic, at the opening character's current level. Existing milestone progress is eligible. These rewards are a prototype and need economy playtesting alongside the difficulty redesign.

The server saves the item and chest consumption in the same versioned command transaction before the animation. The reveal is cosmetic, skippable and respects reduced motion. Full bags and opening away from town are rejected without consuming the chest. No save import is available on the website.

## Multiplayer correctness before launch

Invite-only parties with server-verified membership and ownership. Persist party turns and rewards, lock/version every shared action, resolve rounds exactly once, and use a unique run/participant reward claim so refreshes, retries and multiple tabs cannot duplicate loot. Never accept client-supplied level, damage, inventory, rolls or scores. Test race conditions, reconnects, withdrawals, full inventory and duplicate claims using two independent accounts. Leaderboards use persisted server outcomes; separate future challenge rankings if rules change.

## Acceptance targets for the larger redesign

Within the first five minutes, a player makes a choice that changes what happens. Within the first session, the pair can collaborate and earn something visibly useful. Test all four solo classes and all class pairs. Compare repeated basic attacks against tactical play; simple spamming must perform materially worse while taught mechanics remain fair. Actual playtests with Andrew and his girlfriend determine whether it is fun; simulations alone cannot establish that.

## Rare equipment: purpose before drop rate

Andrew specifically wants rare drops worth pursuing without replaying a ten-second encounter thousands of times. Proposal: named items with one distinctive mechanic (for example a shield that creates a partner-protection option or a staff that changes an elemental combo), not merely a new color and +1 power. Restrict each to a suitable dungeon boss or substantial optional objective. Reveal its source in a collection book. Award bound crafting fragments for every qualifying clear, with a guaranteed unlock after a tuned number of meaningful clears, alongside a chance of an early drop. Show progress openly. Let duplicates become useful fragments; offer a plain coin-sale option on the chest reveal with confirmation. Do not award fragments for replaying a completed room or from trivial enemies. Tune completion times, pair difficulty, pity threshold and economy through playtests before introducing named items to ranked progression. No arbitrary drop rate or number of required runs is final yet.
