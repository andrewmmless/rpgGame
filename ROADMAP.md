# Wayfarer — Full Development Roadmap

*A two-player co-op RPG (Java), built for fun. This document consolidates the current state of the game, everything planned to add, and the reasoning behind the build order.*

---

## 1. Current State (as of this build)

**Classes:** Warrior, Mage, Cleric, Rogue — each with 4 unlockable abilities.

**Town (Hearthglen):** Clickable hub with an inn, blacksmith (weapons/armor), potion shop, and quest noticeboard.

**Regions:** Whispering Woods, Stonefang Caves, Forgotten Ruins, Dragon's Spire.

**Combat:** Turn-based — attacks, abilities, defending, potions, escaping, status effects, boss attack warnings.

**Equipment:** Weapon/armor slots, sorting, comparisons, upgrades, selling. Weapon attributes: Piercing (damage), Siphon (healing), Focus (resource recovery).

**Progression:** Animated milestone chests (Keep/Sell choice), quests, milestones, Endless Tower after the dragon.

**Co-op:** Shared boss dungeon, invite codes, coordinated turns, partner protection/healing, individual rewards.

**Accounts:** Auto-save, leaderboard, server-controlled rewards, blocked save imports.

**Developer Lab:** Separate unranked testing environment — unlocked areas/gear/levels/classes, without touching the live account.

**Status:** This roadmap describes the target. See COWORKER-HANDOFF.md for completed work and release status; do not assume local changes are live.

**Not yet built:** subclasses, gathering professions, crafting, shared housing, guilds, branching multiplayer dungeons, deeper endgame redesign.

---

## 2. Build Order — Blocks 1 through 10

The order is based on dependency, not just importance: fix friction first, build the systems other features rely on, then build the features that consume those systems, then add polish.

### Block 1 — Core QoL & Account Infrastructure

Usability items are implemented. Character slots and expanded death penalties remain pending.
- Shorter party/invite codes — 5-6 alphanumeric characters instead of 16 digits (readable, copy-pasteable).
- "Who still needs to act" turn indicator in co-op combat.
- Post-dungeon loot summary screen — everything earned (items, gold, XP, drops) in one place, not just scroll-back through combat logs.
- Mass selling of items.
- Sell-price rebalancing (currently valued too high).
- Class-specific weapon names.
- **Multiple character save slots per account** (see Section 3).
- **Death penalty system** (see Section 3).

### Block 2 — Progression Foundation
- Attribute system + level-based attribute points.
- Ability leveling tied to the above.
*(Everything in Block 3 onward depends on this existing first.)*

### Block 3 — Loot & Build Depth
- Legendary items (rolls off the attribute system).
- Rare boss drops.
- Spell/ability variety — choosing which abilities to unlock, equipping different move loadouts.

### Block 4 — Endgame Content
- **New Game+ / Nightmare difficulty** on the existing four regions — highest payoff for lowest cost, since it reuses everything already built.
- A second raid-style boss dungeon with different mechanics than the dragon (positioning puzzle, phase changes, enrage timer, etc.).
- A weekly rotating challenge dungeon — same map, randomized modifiers (no potions, double enemy speed, etc.) for repeat value.

### Block 5 — PvP
- 1v1 Duel mode (no matchmaking needed for a 2-player game — a "Duel" option in town or via challenge).
- A separate PvP balance pass — PvE numbers (big enemy HP pools, steady player damage) will not be fair 1v1 as-is.
- Ranked ladder using the existing leaderboard system.
- Optional cosmetic-only rewards on duels.
- *Note: lower priority — with only two players, there's no ladder to climb against strangers. Build the cheapest version possible unless it's specifically fun for the two of you.*

### Block 6 — World Personality
- NPC dialogue that references player name/level, small recurring jokes.
- Flavor text on items and enemies.
- Random world events — limited-time merchant, rare spawn, small festival quest.

### Block 7 — Co-op Depth

Partner rescue is now implemented: one per run, 60-second window, 35% restored health. Combo attacks and bond progression remain pending.
- **Combo attacks** between classes (e.g., Mage sets an enemy on fire, Rogue's next hit detonates it) — arguably the single highest-value addition for making this feel like real co-op rather than parallel play.
- **Rescue mechanic** — a limited window to revive a downed partner mid-fight instead of an auto-loss.
- A "bond" meter or duo-only ability that strengthens the more the two of you play together.
- Simple combat banter/reaction lines on crits, near-deaths, boss kills.

### Block 8 — Narrative & Moments
- Randomized boss attack patterns so fights don't feel solved after one run.
- Short story beats when entering a new region or beating a boss.
- Run journal / highlight reel of big moments — pairs well with the Block 1 loot summary screen.
- **Light story throughline and rival system** (see Section 3).

### Block 9 — Gathering & Crafting
- Skills: mining, foraging (wood), fishing.
- Fishing/mining/foraging nodes tied into the *existing* four regions by difficulty tier (Whispering Woods = tier 1, Stonefang = tier 2, Forgotten Ruins = tier 3, Dragon's Spire = tier 4) rather than building dedicated new towns per skill.
- Optional: one endgame-only gathering node (post-dragon) for rare materials, mirroring the Endless Tower gate.
- Recipes that turn gathered materials into weapons/armor.
- *Note: this is the largest scope item on the list for the smallest player base. If trimming, cut to one gathering skill with a small recipe list rather than a full three-skill economy.*

### Block 10 — Cosmetics & Extras
- Cosmetic items.
- Emotes / simple reaction animations.
- A joke/cursed item (a running gag between the two of you).
- A hidden easter egg personal to the two of you.

---

## 3. Detailed Design Notes

### 3.1 — Multiple Character Save Slots
- **Problem:** currently one account = one character, forcing a new account per class.
- **Solution:** account → list of character slots (cap at 4, one per class), with a character-select screen after login.
- **Per-character:** gold, gear, level, XP, inventory — kept separate so each class has its own progression identity.
- **Account-wide:** leaderboard identity, earned cosmetic unlocks, Developer Lab access — no reason to re-earn these per character.

### 3.2 — Death Penalty (substantial, not game-ending)
- **EXP loss:** lose 10-20% of progress toward the next level, floored at the start of the current level — you lose recent progress, never delevel below where you already were.
- **Item loss:** never touch equipped gear (losing an earned legendary on death would fight directly against the Block 3 loot systems). Instead: drop a portion of unequipped consumables/materials, plus a small chance to drop one random unequipped item.
- **Scale by context:** lighter penalty for early-region deaths, heavier in the Endless Tower / raids, so early mistakes aren't punished as hard as endgame risk-taking.
- **Co-op scope:** penalty applies only to the player who died, not the whole party — this also keeps the Block 7 rescue mechanic meaningful (a rescue *prevents* the penalty, rather than the penalty happening regardless).

### 3.3 — Rival System (Pokemon-style recurring fights)
- **Frequency:** cap at 4-5 total encounters — end of each region as a gate to the next, one before Dragon's Spire, and an optional post-dragon rematch for closure. More than that stops feeling special.
- **Scaling — mirror-match approach:** give the rival the same class/ability/attribute system players use, run by AI. This makes balance mostly automatic (the rival scales the way a player character would) and reuses existing class code instead of needing bespoke boss tuning at each checkpoint.
- **Vary each encounter's shape**, not just its numbers:
  1. End of Whispering Woods — winnable but close; establishes the rivalry.
  2. End of Stonefang — rival has a new ability/gear tier.
  3. End of Forgotten Ruins — rival uses a different class or a build that counters the player's, signaling adaptation.
  4. Pre-Dragon's Spire — hardest regular encounter, full kit.
  5. (Optional) Post-dragon rematch — strongest version, framed as narrative closure rather than a gate.
- **Co-op handling:** have the rival fight the party as a unit, scaled to combined party level — simpler than gating 1v1 per player, and keeps it a shared story beat.
- **Interaction with the death penalty:** exempt rival fights from the normal death penalty, or place a checkpoint immediately before the fight — otherwise a loss both costs progress *and* blocks advancement, which reads as punishing rather than substantial.

### 3.4 — Story / Narrative Approach
- **Scope:** a light throughline, not a branching plot. No dialogue trees, no multiple endings — full branching state tracking is a large cost that competes directly with the actual gameplay systems above.
- **One overarching cause** ties the regions together (e.g., the dragon is the reason Whispering Woods wildlife is corrupted, Stonefang is unstable, the Ruins are ruins) — mostly a framing/flavor-text pass over content that already exists.
- **Fixed story beats** at region entry, milestones, and boss kills — a few lines of text each. This is the actual implementation of "a story" — treat it as part of Block 8, not a separate system.
- **The rival system doubles as narrative** — the cheapest way to make the story feel like it's progressing across regions instead of just existing as flavor text at each stop.
- **No forks** — one fixed path. Revisit branching only if the game is still actively being expanded later and the appetite is there.

### 3.5 — Other "Fun Factor" Additions (mostly writing/tuning, not new systems)
- Randomness *during* exploration, not just in fights — ambushes, hidden treasure rooms, trap choices (take damage for a shortcut vs. play safe).
- Small choice points with real consequences (3-4 across the whole game is enough) — which NPC you unlock, which item you get, how a fight plays out.
- Humor in the writing — funny enemy names, a death message with personality instead of a flat "You died," joke item descriptions.
- A boss you're meant to lose to once (scripted), then beat later once stronger — makes revisiting old content feel like a payoff.
- **Pacing check:** explicitly test whether grinding ever becomes necessary. Fast, satisfying early leveling and a slower but still eventful late game beats a flat grind curve.

---

## 4. Priority Summary (if forced to pick)

**Core / do these regardless of scope:**
Block 1 (QoL + save slots + death penalty) → Block 7 combo/rescue mechanics → Block 2-3 (attributes, legendaries, spell variety) → Block 4 New Game+.

**High value, low cost — do if time allows:**

**Lower priority — build a cheap version or skip:**
PvP (Block 5) — no ladder to climb with only two players.
Full gathering/crafting economy (Block 9) — trim to one skill with a small recipe list rather than three skills with a full economy, unless there's specific appetite to build it out further.

**Skip unless the game is still being actively expanded much later:**
Guilds, shared housing, subclasses — systems designed for populations, not two players.
