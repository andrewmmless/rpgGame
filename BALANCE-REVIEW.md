# Campaign balance checkpoint

A bounded deterministic audit, not a claim that every build or playstyle is balanced. Each class started fresh, completed the twelve routes once, spent earned attribute points, equipped earned drops, restocked up to three potions when affordable and used healing/counters. The sealed-inquiry branch was selected. No developer stats, repeated patrol grind, skill-point upgrades or legendary equipment were used.

Before tuning: the Warrior completed the campaign at level 44. Mage, Cleric and Rogue failed at the introductory guardian. Changes: first-clear rewards close the gap to the next route’s recommended starting level (finale targets 60); repeat rewards remain unchanged. The introductory guardian is capped at one level above the player, up to its original level five. Regional heavy attacks add a guardable 25% maximum-health true-damage impact so armour alone cannot erase the warning. Existing saves can claim campaign catch-up training in the capital, capped by completed story.

Final run results:

```text
WARRIOR
Old Timberline: entry level 1, exit 5, combat turns 16, COMPLETE
Hollow Reach: entry level 5, exit 10, combat turns 16, COMPLETE
Blighted Grove: entry level 10, exit 16, combat turns 10, COMPLETE
Entrance Tunnels: entry level 16, exit 21, combat turns 14, COMPLETE
Deep Shaft: entry level 21, exit 26, combat turns 12, COMPLETE
The Fracture: entry level 26, exit 31, combat turns 12, COMPLETE
Outer Colonnade: entry level 31, exit 36, combat turns 10, COMPLETE
Sunken Hall: entry level 36, exit 41, combat turns 13, COMPLETE
The Drowned Archive: entry level 41, exit 46, combat turns 11, COMPLETE
Ashen Approach: entry level 46, exit 51, combat turns 14, COMPLETE
The Scarred Path: entry level 51, exit 56, combat turns 14, COMPLETE
The Wyrm's Hollow: entry level 56, exit 60, combat turns 13, COMPLETE
Total combat turns=155 deaths=0

MAGE
Old Timberline: entry level 1, exit 5, combat turns 15, COMPLETE
Hollow Reach: entry level 5, exit 10, combat turns 14, COMPLETE
Blighted Grove: entry level 10, exit 16, combat turns 10, COMPLETE
Entrance Tunnels: entry level 16, exit 21, combat turns 14, COMPLETE
Deep Shaft: entry level 21, exit 26, combat turns 12, COMPLETE
The Fracture: entry level 26, exit 31, combat turns 7, COMPLETE
Outer Colonnade: entry level 31, exit 36, combat turns 9, COMPLETE
Sunken Hall: entry level 36, exit 41, combat turns 10, COMPLETE
The Drowned Archive: entry level 41, exit 46, combat turns 8, COMPLETE
Ashen Approach: entry level 46, exit 51, combat turns 10, COMPLETE
The Scarred Path: entry level 51, exit 56, combat turns 11, COMPLETE
The Wyrm's Hollow: entry level 56, exit 60, combat turns 12, COMPLETE
Total combat turns=132 deaths=0

CLERIC
Old Timberline: entry level 1, exit 5, combat turns 28, COMPLETE
Hollow Reach: entry level 5, exit 10, combat turns 18, COMPLETE
Blighted Grove: entry level 10, exit 16, combat turns 13, COMPLETE
Entrance Tunnels: entry level 16, exit 21, combat turns 14, COMPLETE
Deep Shaft: entry level 21, exit 26, combat turns 16, COMPLETE
The Fracture: entry level 26, exit 31, combat turns 18, COMPLETE
Outer Colonnade: entry level 31, exit 36, combat turns 13, COMPLETE
Sunken Hall: entry level 36, exit 41, combat turns 16, COMPLETE
The Drowned Archive: entry level 41, exit 46, combat turns 15, COMPLETE
Ashen Approach: entry level 46, exit 51, combat turns 19, COMPLETE
The Scarred Path: entry level 51, exit 56, combat turns 19, COMPLETE
The Wyrm's Hollow: entry level 56, exit 60, combat turns 15, COMPLETE
Total combat turns=204 deaths=0

ROGUE
Old Timberline: entry level 1, exit 5, combat turns 15, COMPLETE
Hollow Reach: entry level 5, exit 10, combat turns 10, COMPLETE
Blighted Grove: entry level 10, exit 16, combat turns 8, COMPLETE
Entrance Tunnels: entry level 16, exit 21, combat turns 13, COMPLETE
Deep Shaft: entry level 21, exit 26, combat turns 9, COMPLETE
The Fracture: entry level 26, exit 31, combat turns 7, COMPLETE
Outer Colonnade: entry level 31, exit 36, combat turns 10, COMPLETE
Sunken Hall: entry level 36, exit 41, combat turns 11, COMPLETE
The Drowned Archive: entry level 41, exit 46, combat turns 11, COMPLETE
Ashen Approach: entry level 46, exit 51, combat turns 14, COMPLETE
The Scarred Path: entry level 51, exit 56, combat turns 13, COMPLETE
The Wyrm's Hollow: entry level 56, exit 60, combat turns 11, COMPLETE
Total combat turns=132 deaths=0
```

All four cleared at level 60. Zero deaths with this deliberate strategy establishes a viable progression path; it does not establish that combat is sufficiently difficult for expert players. Cleric took more rounds. Further player feedback should focus on repetitive turns, which warnings were understandable, healing availability and rewards that felt worth pursuing.

Co-op: an isolated two-account HTTP story run passed reconnect/pending-move persistence and duplicate reward rejection. A separate prepared Warrior/Cleric mechanics test completed each regional finale, including the witness escort branch. This is not a full organic two-player campaign or an exhaustive equipment/economy audit.

Manual reproduction after Maven package: compile tests/CampaignAudit.java against target/classes into a temporary directory, then run CampaignAudit with WARRIOR, MAGE, CLERIC or ROGUE on that classpath. The audit uses only in-memory game sessions.
