# Wayfarer — Cindergard story and implementation path

## The promise
You enter crown service as a junior member of a frontier relief company. Cindergard is a working kingdom: villages need food, miners need safe roads, nobles owe soldiers, and the crown cannot defend every border alone. You and your partner earn responsibility together through useful deeds. Noble-era adventure and court rivalry provide the tone; no chosen-one prophecy or instant royal favour.

The founding houses earned their standing fighting dragons, then swore a pact that ended the war. Ashwing has broken that pact. Someone at court has also been stripping its boundary wards for prestige and military power. Monsters are being driven toward settlements as the wards fail. Human wrongdoing explains the crisis without excusing the dragon's attacks.

The core question: what makes someone worthy of serving a kingdom—inheritance, obedience, or protecting its people?

## Recurring cast
- Captain Elin Ward: your commanding officer. Practical, answerable for the supply road and every recruit she sends down it. Missions and promotions come through her, not a noticeboard.
- Tessa Reed: fellow recruit and courier. Knows the civilians behind each request. A capable friend, never just someone to rescue.
- Cedric Ashford: younger son of a founding house. Skilled, proud and desperate to restore his family's standing. Initially measures you by birth; eventually must choose his house's reputation or the people it serves.
- Mara and Iven: the existing innkeeper and blacksmith. Civilian consequences arrive through their conversations, requests and changing circumstances.
- Keeper Rowan Flint: Stonefang's mining records keeper. Recognizes that the missing stone is ward material, not ordinary ore.

## Path through the kingdom
1. Hearthglen muster: relief wagons have stopped arriving. Elin commissions you to secure the eastern road. Tessa introduces the people depending on it; Cedric calls it work beneath a noble officer.
2. Whispering Woods: Old Timberline protects supply wagons; Hollow Reach investigates abandoned watchposts; Blighted Grove confronts the displaced Alpha Wolf. Evidence shows a royal ward was deliberately removed. First promotion: Knight / Battle Mage / Field Healer / Ranger. Cedric's first supervised contest is about protecting a convoy, not killing each other.
3. Stonefang Caves: Entrance Tunnels restores access for miners; Deep Shaft escorts a recovery team; The Fracture breaks the Orc Chieftain's blockade. Rowan links missing ward-stone to a sealed noble requisition. The Hollow Vein becomes a future mining site. Cedric finds his family seal on the order. Second promotion follows the regional clear.
4. Forgotten Ruins: Outer Colonnade secures an evacuation route; Sunken Hall recovers testimony; The Drowned Archive reveals the pact's obligations and the cost of dismantling its wards. The Lich Acolyte is exploiting that damage. Cedric initially tries to suppress the evidence, then helps save witnesses. The archive later supports fishing without requiring repeated story scenes. Third promotion follows the regional clear.
5. Dragon's Spire: Ashen Approach establishes a supply camp; The Scarred Path holds it against an assault; The Wyrm's Hollow reaches the ruined pact sanctuary. Cedric publicly chooses crown service over his house's cover-up. The player company confronts Ashwing. The final regional victory earns the fifth class rank and the separate Dragonbane title.
6. Return to service: a modest court recognition and scenes with the people helped along the road. Joint completion can earn a shared remembrance. Optional future threats and gathering remain available; Tower is a side activity, not the story's destination.

## Rules for every chunk
- Story first clears unlock new routes and gear ceilings; XP alone never bypasses them. Existing open routes stay replayable indefinitely.
- Story scenes appear once, with a journal recap/replay. Repeat hunts have short alternate context rather than repeating the same rescue.
- Give a player a reason to act: defend a wagon, reach a signal post, interrupt a ritual. Add those mechanics before claiming they exist.
- NPC dialogue supplies meaningful missions; noticeboards only supply optional small jobs.
- Class ranks use WorldNames exactly. Rank display currently follows region clears; future ceremonies acknowledge those milestones without removing existing ranks.
- Rare rewards should come from varied multi-encounter objectives, with progress toward a guaranteed reward rather than thousands of ten-second repetitions.
- Co-op is cooperation within the same company. Reward each participant once; advance only eligible personal story states. A player may help on earlier chapters without skipping their own story.
- Rival contests cannot remove XP or possessions. No casino work in these chunks.
- Keep old characters, region clears and gear. Make catch-up dialogue available instead of forcing completed content to replay.

## Small implementation chunks and acceptance checks
1. **Muster and first commission (this pass):** persistent Elin dialogue, accept the road mission, report the Woods clear; map presents the current mission. No new combat or fake siege features. Existing Woods veterans can report their clear after accepting. Check persistence and repeated reports.
2. **Playable Woods routes:** implement the three sub-areas and their server-enforced first-clear gates. Keep Old Timberline freely available; introduce entry/mid/hard encounter ranges and regional gear bands. Carry old Woods clears forward. Check route gates and save restoration.
3. **Woods objectives and cast:** convoy defense, watchpost investigation, Tessa and Cedric scenes, Iven's ward fragment quest. Optional combat choices alter costs, not permanent access. Check one objective branch and one reward claim.
4. **Woods conclusion and duo support:** Alpha Wolf scene, report, promotion ceremony and shared eligible progress. Add a visible regional reward target. Check two-player reward idempotency and different story states.
5. **Progression choices:** attributes, ability upgrades/loadouts and boss reward crafting progress. Fit strength to the story tier. Existing investments remain valid. Check point spending and equipment ceilings.
6. **Stonefang arc:** reuse route/objective infrastructure; add Rowan, the requisition and second rival encounter. Implement the mining location first; profession mechanics later.
7. **Ruins arc:** evacuation, pact evidence and Cedric's choice; add the archive and third promotion scene.
8. **Spire and ending:** camp defense, final confrontation and Dragonbane recognition; then review the complete campaign before adding optional endgame.

Current limits: chunks 2–8 are planned, not implemented. The opening commission is layered onto the existing Woods expedition until the three-route chunk replaces it. No new time-based attacks or forced daily schedule. Each development pass should finish a small usable slice and run focused checks, not repeated campaign simulations.

## Playable campaign checkpoint

The twelve named routes are now playable solo expeditions with distinct level bands, guardian encounters, NPC mission introductions and first-clear conclusions through the court ending. Each route keeps the existing three encounters, supply choice and guardian structure. This is the complete linear story path; bespoke convoy/evacuation mechanics, rival duels and synchronized co-op story objectives remain future enhancements, not implemented systems.

Woods: 1–15. Caves: 16–30. Ruins: 31–45. Spire: 46–60. Each route covers about five levels. First clears award twice the XP requirement of the route's minimum level; repeat patrols award ordinary fight rewards. Guardians use the upper end of the route range, so first-clear rewards do not eliminate preparation or hunting. Epic drops start at level 31; early-route items stay capped to their route. Existing region clears unlock their three routes automatically, and active legacy expeditions can finish normally.

The illustrated kingdom map shows four clickable regions and only the selected region's three routes. Story dialogue and the journal archive are expandable. Each route unlocks its successor once cleared; character level alone cannot skip them.

Validation: full package build and 21 tests, including all twelve routes with save restoration between actions, route-lock rejection, first-clear reward idempotency, early loot ceilings and legacy region-clear access. This is an initial balance pass; long-term pacing and mobile visual polish still need player feedback.

### Shared story and world-map checkpoint
All twelve routes now support three-stage two-player missions with escort, investigation or ward-disruption objectives and individual persistent story progression. The central capital contains services, guild hall and dungeon lodge; gathering sites have basic skill/material progression. Guilds now hold eight members while dungeon parties remain two. See WAYFARER-HANDOFF.md for full mechanics and verification. Future kingdoms, wars and local conflicts are recorded for later, not implemented now.
