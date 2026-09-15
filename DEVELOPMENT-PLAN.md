# Wayfarer — Full Development Plan (for Codex handoff)

*A two-player co-op RPG (Java), built for fun. Consolidates current game state, naming, core design philosophy, and the full build order with reasoning.*

---

## 0. Governing Design Rule — Grind/Story Relationship

**This rule governs every system below and should be treated as a constraint on all of them, not a separate feature.**

- Grinding (leveling, gear farming, ability building) is **always available, on the player's own schedule** — no forced pacing, no requirement to progress the story first.
- Within the player's **currently unlocked tier**, they can freely grind to full mastery of what's available at that tier (best gear, maxed abilities, optimal build).
- **Clearing a story milestone raises the ceiling** — unlocks new sub-areas, new gear tiers, new rank-ups, and new quests. Story progress is a key that opens more room to grind, not a leash forcing the player through it.
- This is why region sub-areas are level-ranged, why rank-ups are milestone-based rather than grind-based, and why the death penalty scales by story tier — all three already implement this rule; new systems should follow the same pattern.

## 1. Quest Design Rule

- Real, story-relevant quests should come from **NPC interaction and dialogue**, not a quest board that hands the player a checklist of 10 quests up front.
- Talking to NPCs (the blacksmith needing a material, the innkeeper mentioning a missing guest, a mining NPC asking for help) is what surfaces meaningful quests — makes the world feel responsive rather than a to-do list.
- The existing **quest noticeboard** in Hearthglen is repurposed for **minor/optional flat tasks only** — bounties, gathering requests, that kind of thing. It is not the source of story-relevant quests.

---

## 2. World Naming

**Kingdom: Cindergard** — founded by (or ruled by) a dragon-slaying lineage. The name is literal: it refers to the ash/embers of dragons the founding family once slew, not just a fire motif. This gives free backstory:
- Explains why the kingdom takes the Dragon's Spire threat personally rather than as a generic regional emergency — it's the dragon that broke the pact the founding family once helped keep.
- Explains why the Knight rank line carries real institutional weight — knighthood traces back to that original dragon-slaying order.
- Gives the rival a plausible personal connection to the same lineage (a rival heir, a disgraced branch, someone trying to earn a name they don't currently hold).
- Sets up the endgame title **"Dragonbane"** (reserved for the actual dragon kill) as a title the founding family once held and the player is the first to earn again in generations.

### Class Rank Lines (tied to Cindergard, milestone-based per region cleared — not grind-based, per the Governing Design Rule)

**Warrior -> Knight line:** Squire -> Knight -> Knight-Lieutenant -> Knight-Captain -> Knight Commander (final rank). Reserve **"Dragonbane"** as a singular title earned only at the dragon kill, separate from the rank ladder.

**Mage -> Battle-Mage/Court line:** Apprentice -> Battle Mage -> Court Mage -> Royal Magus -> Archmagus (final rank). Optional top title: **"Cinderweaver"**.

**Cleric -> Healer line:** Acolyte -> Field Healer -> Court Healer -> High Healer -> Grand Healer (final rank). Optional top title: **"Lightwarden"**.

**Rogue -> Scout line:** Scout -> Ranger -> Royal Scout -> Spymaster -> Shadow Warden (final rank).

All read cleanly with "of Cindergard" appended (e.g., "Knight-Captain of Cindergard").

### Region Sub-Areas (level-gated zones within each existing region)

- **Whispering Woods:** Old Timberline (entry) -> Hollow Reach (mid) -> Blighted Grove (hardest)
- **Stonefang Caves:** Entrance Tunnels (entry) -> Deep Shaft (mid) -> The Fracture (hardest)
- **Forgotten Ruins:** Outer Colonnade (entry) -> Sunken Hall (mid) -> The Drowned Archive (hardest; also the fishing zone)
- **Dragon's Spire:** Ashen Approach (entry) -> The Scarred Path (mid) -> The Wyrm's Hollow (final, pre-dragon)

### Gathering Areas

- **Mining (Stonefang):** the Hollow Vein
- **Wood-foraging (Whispering Woods):** the Blighted Hollow
- **Fishing (Forgotten Ruins):** the Drowned Archive (doubles as the hardest combat sub-area and the fishing zone)

*All names above use the same convention as the existing built locations (Hearthglen, Whispering Woods, Stonefang Caves, Forgotten Ruins, Dragon's Spire): plain-English compound words, no invented fantasy syllables. Andrew is open to renaming any of the above later -- treat as working names.*

---

## 3. Current State (as of this build)

**Classes:** Warrior, Mage, Cleric, Rogue -- each with 4 unlockable abilities.

**Town (Hearthglen):** Clickable hub with an inn, blacksmith (weapons/armor), potion shop, and quest noticeboard.

**Regions:** Whispering Woods, Stonefang Caves, Forgotten Ruins, Dragon's Spire.

**Combat:** Turn-based -- attacks, abilities, defending, potions, escaping, status effects, boss attack warnings.

**Equipment:** Weapon/armor slots, sorting, comparisons, upgrades, selling. Weapon attributes: Piercing (damage), Siphon (healing), Focus (resource recovery).

**Progression:** Animated milestone chests (Keep/Sell choice), quests, milestones, Endless Tower after the dragon.

**Co-op:** Shared boss dungeon, invite codes, coordinated turns, partner protection/healing, individual rewards.

**Accounts:** Auto-save, leaderboard, server-controlled rewards, blocked save imports.

**Developer Lab:** Separate unranked testing environment.

**Status:** Co-op update pushed to origin and live.

**Not yet built:** subclasses, gathering professions, crafting, shared housing, guilds, branching multiplayer dungeons, deeper endgame redesign.

---

## 4. Build Order -- Blocks 0 through 9

### Block 0 -- Bug Fixes & Difficulty Rebalance
*First because none of the content below matters if the base game is broken or trivially easy.*
- Fix bulk-sell scroll-position bug (capture/restore scroll position or last-visible-index around the sell action).
- Add "sell all rare" bulk-sell filter alongside the general one.
- Rework leveling so it adds only the stat increase to HP/mana instead of full-restoring -- single biggest lever on difficulty.
- Slow the XP curve so leveling isn't blowing past content.
- Fix the level-25 item cap so gear can keep pace with a slower, longer leveling curve.
- Design real enemy threat (attack patterns that punish spam-attacking, e.g. an attack that hits harder the more consecutive turns the player attacks without defending).
- Build region sub-areas (level-gated zones within each region, per naming in Section 2) -- a difficulty fix and the foundation for everything in Block 2 onward.
- Add a balanced auto-attack toggle: basic attacks only (never abilities/potions), cancels immediately on boss attack telegraphs, toggled per-fight rather than a permanent setting.

### Block 1 -- Account & Economy Infrastructure
*Foundational systems that don't depend on content.*
- Multiple character save slots per account (cap 4, one per class) -- per-character gold/gear/level/XP; account-wide leaderboard identity, cosmetic unlocks, and Developer Lab access.
- Death penalty on death: % of current-level XP progress lost (never delevel below the current level's floor), unequipped items/materials at risk (never equipped gear), scaled lighter early/harsher endgame, applies only to the player who died in co-op. **Rival fights are exempt from this penalty** (or gated by a checkpoint immediately before the fight) so a loss doesn't stack "lose progress" on top of "blocked from advancing."
- Mass selling, sell-price rebalancing, class-specific weapon names.
- Shorter party/invite codes (5-6 alphanumeric characters).
- "Who still needs to act" turn indicator + post-dungeon loot summary screen.

### Block 2 -- Progression Foundation
*Everything from here on assumes attributes and rank exist.*
- Attribute system + level-based attribute points.
- Ability leveling.
- Kingdom rank/subclass system per Section 2 -- tied to region-clear milestones, not grinding.
- Legendary items (rolls off Block 2's attributes).
- Rare boss drops.
- Spell/ability variety -- unlockable spells, selectable move loadouts.

### Block 3 -- Story & Content Expansion
*Built on Block 0's sub-areas -- this is where "a lot more content" and the longer story happen.*
- Prologue arc in Hearthglen -- the kingdom deputizes the player; introduces recurring NPCs (commanding officer, fellow recruit, the rival before they're openly a rival).
- Whispering Woods arc -- full sub-area sweep, wood-foraging area (the Blighted Hollow) woven in, first rival encounter, first rank-up.
- Stonefang Caves arc -- sub-area sweep, mining area (the Hollow Vein), second rival encounter, second rank-up, a new recurring NPC (mining-history character).
- Forgotten Ruins arc -- sub-area sweep, fishing/drowned-archive area, third rival encounter, third rank-up, deeper lore reveal about who/what broke the original pact.
- Dragon's Spire arc -- final rank-up, final rival confrontation, confrontation with the source of the corruption.
- Overarching cause tying every region together: the broken pact between Cindergard and the dragons, spreading outward from Dragon's Spire, with the founding dragon-slaying lineage as the throughline.
- Story-earned titles (region-clear titles, "Dragonbane" reserved for the dragon kill, a possible shared/couple title for joint milestones) layered on top of the rank system -- narrative reward, not a second grind track.
- All quests within this block should be delivered via NPC dialogue, per Section 1 -- not a checklist quest board.

### Block 4 -- Endgame Content
- New Game+/Nightmare difficulty on the four regions (cheapest, highest payoff).
- A second raid-style boss dungeon.
- Weekly rotating challenge dungeon.

### Block 5 -- PvP
- 1v1 Duel mode, separate PvP balance pass, ranked ladder via existing leaderboard, optional wager/cosmetic reward.
- Flagged lower priority -- with two players there's no ladder to climb against strangers; build the cheapest viable version.

### Block 6 -- World Personality & Co-op Depth
- NPC dialogue, flavor text, random world events.
- Combo attacks between classes, rescue mechanic on downed partner, bond meter/duo ability, combat banter.

### Block 7 -- Gathering & Crafting Systems
*The mining/foraging/fishing areas themselves are built in Block 3 as story locations -- this block is the skill/recipe mechanics layered onto those already-built areas.*
- Mining, foraging, fishing skill mechanics and leveling.
- Recipes converting gathered materials into weapons/armor.

### Block 8 -- Cosmetics & Extras
- Cosmetic items, emotes, joke items, hidden personal easter egg.
- Bring back the casino (blackjack with proper Ace handling, rob-gas, rob-bank, coin flip -- largely already built from an earlier version of this project) as a Hearthglen location. **The casino must kick the player out once they hit zero money.**

---

## 5. Why This Order

Block 0 fixes the foundation so nothing built after it inherits broken difficulty or bugs. Block 1 sets up accounts/economy since later blocks assume they exist. Block 2 builds the systems (attributes, rank) that Block 3 depends on for balance and milestone structure. Block 3 is placed right after progression exists because the sub-areas and rank-ups it needs are already in place by then -- deliberately the largest block since it's where "more areas, more interactions, longer story" lives, governed throughout by the Grind/Story rule in Section 0. Everything after Block 3 (endgame, PvP, co-op depth, gathering mechanics, cosmetics) is optional polish that can be reordered or trimmed without breaking anything earlier.

## Implementation checkpoint

This document is the target design, not a claim that all listed behavior exists. Guilds, shared housing, bond rewards, rescue, short co-op codes, turn readiness and reward summaries are already implemented. Four character slots are now implemented; difficulty/sub-area work in Block 0 is next. Casino is deferred to Block 8, with no implementation authorized in this checkpoint.

Character slots: existing saves occupy slot 1 automatically. Each account has four slots, one per class. Gold, gear and progression are independent; guild/bond membership and Developer Lab access remain account-wide. The leaderboard currently shows the active character under the account identity. Leave your co-op party before switching. Existing solo encounters resume when returning to their character. There is no character deletion UI yet.

Storage: hearthglen.rpg_saves holds the active character, rpg_character_slots holds inactive snapshots, and rpg_slot_state tracks the selection. Switching is transactional; stale character commands are rejected. New tables are additive and existing saves need no manual conversion.

### Difficulty and equipment pass

Implemented: level-ups add only the increase in maximum health/resource; XP requirements now grow from 50 using 20 per level plus a small quadratic term; heavy attacks use 2.6x damage and defending restores six extra resource; Tower gear uses player/encounter level instead of the region-25 ceiling; bulk sales gain a rare-item selector and preserve the open panel and page position on redraw. Existing levels and items are unchanged. Existing XP values remain valid, though the percentage toward the next level is lower.

Focused verification: level-up recovery, legacy XP acceptance, defend versus heavy strike, and atomic/protected bulk selling. Balance is an initial tuning pass, not a full playtest. Sub-areas, per-fight auto-attack and deeper enemy patterns remain for the next pass.

### Naming applied

Cindergard appears in the welcome, sign-in description and world screen. Character ranks use the exact class ladders above, derived from consecutive region clears, not level. Dragonbane is a separate title requiring the Dragon's Spire clear. WorldNames centralizes all planned sub-area and gathering names; these names do not yet represent playable new locations. Wayfarer remains the game title and Hearthglen the starting town.

### Kingdom navigation and unlocks

The Adventure screen now starts on Cindergard's region map. Hearthglen is an optional services destination. Geography follows the woodland road into Stonefang's tunnels, emerging in the royal ruins and ascending Dragon's Spire. Consecutive region boss clears unlock routes; level ranges are recommendations. Shop equipment is capped to the highest unlocked region, as regional loot already was. Cleared regions remain replayable. Tower is tucked under an optional challenge after the campaign.

Still pending: playable sub-areas, prologue/NPC story dialogue and quests, authored rival encounters, and expanded region arcs. The current boss-clear campaign is the migration foundation for these features, not the completed story.
