# Wayfarer social release — September 15, 2026

## Delivered in this batch

Private two-account guilds with names, four emblems, renewed eight-character invitations and membership checks. Founding is free. The founder may leave after the other member leaves; an empty guild is retired and contributions are not refunded.

A shared illustrated cottage, three improvements, pooled coin contributions (10/25/50), an activity ledger, five furniture placement slots and a small decoration catalog. Guild mates may spend guild funds and decorate. Contributions debit only the donating player's normal character in town. Furnishings remain owned when replaced. Both members must complete a qualifying adventure in the same guild to earn house supplies and the Warden trophy.

Pair-specific bond progress, independent of the guild. Three milestones: Trail Companions at 1 credited clear, Rally at 3, and Homeward Bound/twin lanterns at 5. Rally restores 10% maximum health and 6 resource to both players once per run. Two simultaneous Rally selections do not stack. Credit requires at least three combat rounds and five minutes since the last credited clear. Every victory still awards normal dungeon loot and records a memory.

Saved shared adventure memories, contributions and preset guestbook reactions with a short posting cooldown. No free-text chat moderation system was introduced.

Region-specific loot names and level caps: Woodland, Stonefang, Relic and Stormforged. Ordinary early-region drops are common; boss rewards remain rare. Epic rolls require item level 10 and are 8% for boss/chest rewards or 2% on ordinary equipment rolls. Ordinary fights still first roll whether equipment drops at all. Milestone chests use the highest unlocked region. The introductory co-op Warden awards rare Woodland equipment capped at level 4. Existing gear is unchanged. Legendary armour is NOT implemented.

## Explicitly deferred after the usage-budget reduction

Friendly PvP, a second town, multi-room co-op adventure, combat art/animation/combo expansion, and random world events. These correspond to remaining fun blocks 4, 6, 7, 8 and 9. The larger progression systems—character slots, attributes, ability leveling, legendary-item chase, endgame expansion—also remain pending. Casino and gambling stay excluded.

## Verification

Focused Rally/region-drop checks and one isolated two-account co-op/social test passed: shared turns, restart, rewards, replay rejection, guild membership, wallet debit, stale version denial, decoration restrictions, outsider denial, guestbook cooldown and persisted house/bond state. JavaScript syntax and Java build checked. No broad simulations or full cross-device visual QA performed; the user requested conserving remaining usage.

## Release

Commit and push rework, then wait for Render to finish building. This document does not assert that the current release is live. Startup adds separate guild, membership, bond and social-lock tables; existing character saves are not migrated. Shared mutations serialize under a short database lock for this small-player-count deployment, keeping contribution/save/reward changes transactional. A larger player population would require finer-grained concurrency design.

Keep Supabase credentials private. No hosting-plan changes were made. The owner Developer Lab setting is separate and must be verified in Render if needed. Normal guild actions cannot use the lab's test coins or characters.
