# Banana Bomb Runner – Backend

Spring Boot 3.5 (Java 21) REST API for the Banana Bomb Runner web game (CIS046-3 Software For Enterprise).
Frontend repo: `banana-bomb-runner-game-frontend`.

> AI-assisted: the first version of much of this code was generated with Claude (Anthropic) in November 2026,
> then reviewed, changed and tested.

## How to run

1. **MySQL:** start MySQL in XAMPP. The database `banana_bomb_runner_db` is made automatically on first run
   (`createDatabaseIfNotExist=true`). Tables are made by JPA.
2. **Email for the OTP code (Brevo or Gmail):** copy `.env.example` to `.env` (same folder as `pom.xml`)
   and fill in `MAIL_HOST`, `MAIL_USER`, `MAIL_PASS`, `MAIL_FROM` and `JWT_SECRET`.
   `.env` is in `.gitignore`, so the key never goes to GitHub.
   You can use environment variables instead (IntelliJ: Run → Edit Configurations → Environment variables):
   ```
   MAIL_USER=your-smtp-login;MAIL_PASS=your-smtp-key;MAIL_FROM=you@gmail.com;JWT_SECRET=a-long-random-secret
   ```
   When the app starts, the log says `Email is set up ...` if the mail details look right.
   The OTP code is **not** printed in the console. Only while testing on your own PC (for example when the
   email does not arrive), you can put `OTP_LOG_CODE=true` in `.env`; the log then shows
   `DEV ONLY - OTP for ... is 123456`. Keep it `false` in the real version.
3. Open the folder in IntelliJ, let Maven download the dependencies, run `BbrApplication`.
4. Check: <http://localhost:8080/api/levels> shows 5 levels as JSON.
   To use another port, set `SERVER_PORT` (for example `SERVER_PORT=8078`) and point the frontend proxy
   in `vite.config.js` at the same port.

Tests (Spock, Groovy): right click `src/test/groovy` → Run 'All Tests', or `mvn test`.

## API

| Method | Path | Login? | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | no | `{email, username, password}` → BCrypt hash saved |
| POST | `/api/auth/login` | no | `{email, password}` → 6-digit code sent by email. 5 wrong passwords → 5 min lock (429) |
| POST | `/api/auth/verify-otp` | no | `{email, code}` → JWT in an HttpOnly cookie |
| POST | `/api/auth/logout` | no | clears the cookie |
| GET | `/api/levels` | no | the 5 levels (speed, distance, bombs, puzzle time) |
| GET | `/api/progress` | yes | `{username, highestLevel, bestScore}` |
| GET | `/api/puzzle?level=N` | levels 1-2: no, 3-5: yes | Banana API puzzle `{puzzleId, image, timeLimit}` (no solution) |
| POST | `/api/puzzle/answer` | levels 1-2: no, 3-5: yes | `{puzzleId, answer}` → `{correct, timeUp, bonus, highestLevel}`. The server unlocks the next level (not for guests) |
| POST | `/api/scores` | yes | `{level, points}` |
| GET | `/api/leaderboard` | no | top 10 players by best score (each player once) |

## Authorization (guest and player)

Authentication (the JWT cookie, `JwtFilter`) says **who** the player is.
Authorization (`AccessPolicy`) decides **what** they may play. The server checks it, so changing the React code
does not get around it.

| Who | Levels they can play | Saved? |
|---|---|---|
| Guest (not logged in) | 1 and 2 (`AccessPolicy.FREE_LEVELS = 2`) | nothing: no progress, no score |
| Logged-in player | 1 and 2, plus every level they have unlocked by solving a gate puzzle | progress, scores, leaderboard |

- A guest who asks for level 3, 4 or 5 gets **401** `Log in to play level N`.
- A player who asks for a level they have not unlocked yet gets **403** `Level locked`.
- `/api/progress` and `/api/scores` still need a login (401 for a guest).

## Themes

| Theme | Where |
|---|---|
| Interoperability | `BananaClient` (Banana API over HTTP + JSON, 5 s timeout), REST + JSON for the React app, SMTP email through Brevo (or Gmail), MySQL |
| Event-driven | `OtpRequestedEvent` → `EmailListener` (`@Async`), `LevelUnlockedEvent` → `UnlockHistoryListener` (`unlock_history` table) |
| Virtual identity | BCrypt, 2-step login (password + 6-digit email OTP, only its hash is saved, 5 min / 5 tries), login lock, JWT in an HttpOnly + SameSite cookie, `JwtFilter`, server-side level unlock, guest vs player authorization (`AccessPolicy`) |
| Version control | `main` + `development`, feature branches, pull requests, GitHub Actions CI (`.github/workflows/ci.yml`), version tags + `CHANGELOG.md` |
