# Put Hearthglen online

The game is packaged as one Spring Boot application: the browser UI and Java API share one origin. Docker and a Render blueprint are included. The remaining external step is connecting your hosting and database accounts.

1. Commit **all** source, resources, tests, Maven wrapper, and deployment files on `rework`, and push that branch. The original V4 commit missed several untracked files; include them now. Never commit data/, saves/, passwords, or .env.
2. Create a Supabase PostgreSQL project (or another persistent PostgreSQL database). Use its **session pooler** connection on port 5432 if the hosting connection needs IPv4.
3. In Render, create a Blueprint from your repository's `rework` branch. The included render.yaml requests a free Docker web service, with no Render database or paid disk.
4. Enter environment values in Render's secret settings, not in source:
   - `DATABASE_URL`: `jdbc:postgresql://YOUR-POOLER-HOST:5432/postgres?sslmode=require`
   - `DATABASE_USERNAME`: your database username (for Supabase's session pooler, commonly `postgres.PROJECT_REFERENCE`)
   - `DATABASE_PASSWORD`: your database password
5. Deploy. The blueprint sets production mode, HTTPS-only cookies, and the bind address. The application refuses production startup without PostgreSQL, SSL configuration, and secure cookies.
6. Verify `/health`, create a test account, finish a fight, redeploy, sign in again, and confirm the save is preserved. Then create your own account and import your console save if desired.

Schema tables are created under **hearthglen**, not Supabase's public schema. Do not add this schema to Supabase's exposed API schemas or grant anonymous roles access to account/save tables. The Java backend is the database client; no database password is sent to the browser.

Free service availability and limits can change; verify the chosen plan before creating services. An ephemeral hosting filesystem cannot safely store online saves, which is why the production profile requires an external database. Local H2 is for development only. PostgreSQL and Docker deployment have not yet been tested against your actual accounts.

## Updates after launch

Develop and test on rework; deploy a reviewed checkpoint. The running website changes when the new application is deployed, and persistent database saves remain. Keep backups before any save-format or schema migration. Sessions currently live in memory, so updates sign players out; their last accepted turn is saved. This release targets one app instance; distributed rate limiting and persistent shared sessions are future scaling work.

Before a broader public launch, add account recovery and review dependency updates, abuse controls, backups, and accessibility with real players. Email recovery needs a mail provider and verified domain; it is not silently simulated here.

Official references: [Render Docker](https://render.com/docs/docker), [Render Blueprints](https://render.com/docs/blueprint-spec), [Supabase PostgreSQL connections](https://supabase.com/docs/guides/database/connecting-to-postgres), [Spring Boot SQL](https://docs.spring.io/spring-boot/reference/data/sql.html).
