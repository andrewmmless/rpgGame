# Handoff checkpoint

User asked for maximum autonomous progress, then requested less repetition in the interface, more engaging combat, and a running leaderboard. User subsequently asked to conserve usage, save work and publish.

Implemented: a Spring Boot browser RPG, four classes with four abilities, campaign quests and four regions, equipment and upgrades, area bosses, guarded/heavy/poison/drain enemy patterns, endless tower, accounts, database autosaving with combat resume, console-save import, export, and a live leaderboard. The revised UI has one character status strip, one destination list, and a focused encounter view. Leaderboards update every 15 seconds; imported and pre-leaderboard preview saves are practice entries. Normal new characters rank by tower, bosses, level, wins. No usernames/passwords are exposed in leaderboard responses.

Code is on rework. Main must stay untouched. Original project: /Users/andrew/Documents/GitHub/rpgGame. Prepared working copy: rpg-web under this ChatGPT project. Local preview: http://localhost:18080. Tests: Maven campaign suite, original 84 foundation checks, and tests/web_smoke.py for real HTTP sessions, database restart persistence, account isolation, and leaderboard eligibility.

Public publishing is blocked at Render sign-in. Render Dashboard currently shows its login page. No hosting or database credentials supplied. Dockerfile, render.yaml, DEPLOYMENT.md are prepared. Do not claim localhost is publicly hosted. Next session: sign into Render with user assistance, connect persistent PostgreSQL (Supabase session pooler or equivalent), set secret env values, deploy rework, and verify a real public URL and restart persistence. Docker/PostgreSQL deployment still needs validation. Do not create paid services without explicit authorization.

Current limits: no email password recovery, fixed expedition structure, shared scenery, no multiplayer, and no in-game JSON-backup restoration. Classic console remains supported but the new campaign is browser-first. Upgrade Boot dependencies before a wider public launch; current verified build uses Boot 4.0.3.
