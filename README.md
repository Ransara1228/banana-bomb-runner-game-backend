# Banana Bomb Runner – Backend

Spring Boot 3.5 (Java 21) REST API for the Banana Bomb Runner web game (CIS046-3 Software For Enterprise).
Frontend repo: `banana-bomb-runner-game-frontend`.

> AI-assisted: the first version of much of this code was generated with Claude (Anthropic) in November 2026,
> then reviewed, changed and tested.

## How to run

1. **MySQL:** start MySQL in XAMPP. The database `banana_bomb_runner_db` is made automatically on first run
   (`createDatabaseIfNotExist=true`). Tables are made by JPA.
2. **JWT secret:** set a long random `JWT_SECRET` environment variable
   (IntelliJ: Run → Edit Configurations → Environment variables):
   ```
   JWT_SECRET=a-long-random-secret-of-32-or-more-characters
   ```
   Without it a default key is used (fine for testing only).
3. Open the folder in IntelliJ, let Maven download the dependencies, run `BbrApplication`.
4. Check: <http://localhost:8080/api/levels> shows 5 levels as JSON.
   To use another port, set `SERVER_PORT` (for example `SERVER_PORT=8078`) and point the frontend proxy
   in `vite.config.js` at the same port.

Tests (Spock, Groovy): right click `src/test/groovy` → Run 'All Tests', or `mvn test`.

## API

| Method | Path | Login? | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | no | `{email, username, password}` → BCrypt hash saved |
| POST | `/api/auth/login` | no | `{email, password}` → JWT in an HttpOnly cookie. 5 wrong passwords → 5 min lock (429) |
| POST | `/api/auth/logout` | no | clears the cookie |
| GET | `/api/levels` | no | the 5 levels (speed, distance, bombs, puzzle time) |
| GET | `/api/progress` | yes | `{username, highestLevel, bestScore}` |
| GET | `/api/puzzle?level=N` | yes | Banana API puzzle `{puzzleId, image, timeLimit}` (no solution) |
| POST | `/api/puzzle/answer` | yes | `{puzzleId, answer}` → `{correct, timeUp, bonus, highestLevel}`. The server unlocks the next level |
| POST | `/api/scores` | yes | `{level, points}` |
| GET | `/api/leaderboard` | no | top 10 players by best score (each player once) |

## Themes

| Theme | Where |
|---|---|
| Interoperability | `BananaClient` (Banana API over HTTP + JSON, 5 s timeout), REST + JSON for the React app, MySQL |
| Event-driven | `LevelUnlockedEvent` → `UnlockHistoryListener` (`unlock_history` table) |
| Virtual identity | BCrypt, login lock, JWT in an HttpOnly + SameSite cookie, `JwtFilter`, server-side level unlock |
| Version control | `main` + `development`, feature branches, pull requests, GitHub Actions CI (`.github/workflows/ci.yml`), version tags + `CHANGELOG.md` |
