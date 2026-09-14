# Andrew's RPG — V4 foundation

Requires JDK 17 or newer. No external dependencies.

```
sh test.sh
java -cp build/classes Main
```

Open this directory as the IntelliJ project. Source remains in `src/` so the existing project layout works.

## Scope

Preserves Warrior, Mage, Cleric, Rogue, the four original areas, all 24 enemies (including Victoria), shops, training, sword upgrades, potions, and the Coke event. No new loot, quest, dungeon, or endgame systems are included. The original permadeath console flow remains.

`Player` is abstract. `PlayerClass` supplies creation and base profiles; each subclass supplies immutable reusable `Ability` definitions. Abilities carry IDs, costs, unlock levels, cooldowns, and composable behavior. KO Slash is reliable burst damage, Fireball burns, Backstab bleeds, and Prayer heals. All classes currently use a shared regenerating resource pool; distinct rage/mana/energy economies are deliberately deferred.

`CombatEngine` owns one encounter, accepts `CombatAction` plus an optional ability ID, and returns an immutable `CombatResult` with outcome, messages, and health values. It has no console, filesystem, Spring, or class-specific checks. `Fight` is the console adapter. A future web controller can call the same engine; serialize requests per encounter. Messages are currently English strings; structured localized event payloads are a future extension.

Invalid abilities, cooldown attempts, insufficient resource, and unusable potions consume no turn. Cooldowns count subsequent accepted actions. Victory rewards occur exactly once; dead enemies never retaliate after a direct killing blow. A defeat takes precedence if both combatants die in a round. A successful escape gives no rewards.

Status definitions are immutable. Each character owns remaining durations, ticks them at the end of its own turn, and refreshes matching IDs rather than stacking. Damage-over-time, regeneration, stun, and guard are supported. Guard halves incoming damage through the next enemy attack; encounter completion clears temporary effects. Reuse the same engine throughout an encounter; constructing a new one resets temporary encounter state.

## Balance

- Level-one HP: Warrior 100, Mage 80, Cleric 95, Rogue 85; +10 each level.
- Attack: 12–15 by class, +3 each level. Armour: 5–12, +2 each level.
- Damage mitigation: `raw * 50 / (50 + armour)`, rounded; zero damage stays zero.
- Typed resistances support weaknesses down to -100% and resistance up to 80%. TRUE bypasses armour and typed resistance.
- Normal enemy HP: `28 + 6*(level-1)`; attack: `9 + 2*(level-1)`; armour: `4 + level`.
- Existing bosses use 1.6x HP, 1.2x attack, and 2x XP. Their mechanics remain simple.
- XP to next level: `30 + 12*(level-1)`; same-level normal reward: `10 + 4*(level-1)`. Three normal wins per level; excess XP carries over. Level cap 100.
- Level-ups restore HP and resource. Three starting potions; each restores 40% maximum HP.
- Tests simulate 4,000 basic-attack-only, same-level normal fights across all classes at levels 1, 5, 10, 16, and 25. Average HP cost is approximately 14–26%, with no deaths. This is a foundation tuning check, not proof of complete campaign or boss balance. Choosing higher-level areas remains dangerous.

## Saves and compatibility

V4 writes atomically to `saves-v4/`. Loading falls back to reading an original `saves/` file, translates fractional HP/XP progress, and derives stats from the new class/level formulas. Original saves are never overwritten or deleted. V4 persists resource, coins, potions, and sword bonus. Saving during combat is not exposed by the console. The full persistence rewrite is outside this phase.

The old direct special-move methods are replaced by abilities. Player stat setters were removed so class and level determine stats consistently.

## Working copy

This rewrite was prepared on a separate local `rework` branch from commit `0a141d0`, using all 15 current source files from the original repository, including Area and TrainingGrounds which initially were untracked. Synced reference files and main were left untouched. No commits or pushes are performed automatically.
