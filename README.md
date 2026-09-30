# Guess the Word (Java / Spring Boot)

A Wordle-style game built with **Java 17, Spring Boot 3, Spring Data JPA and H2**.
This is a from-scratch Java port of the Python/Flask version, built to the same
specification, with the same rules, the same 20 seed words, and the same admin
reports.

## Features
- Register / log in (username: 5+ letters; password: 5+ chars with a letter, a digit
  and one of `$ % *`); duplicate usernames are rejected case-insensitively
- 20 five-letter words seeded on first run; a random one is chosen per game
- Max 3 words per user per day, max 5 guesses per word, upper-case guesses only
- Guesses must be real English words (checked against a bundled ~15k-word list);
  an invalid word is rejected without using up a guess
- Green / orange / grey letter feedback, win / lose messages with an OK button,
  guesses preserved in order (an unfinished game survives a page refresh)
- Every word given and every guess made is saved with the date
- Admin reports: daily summary (users who played, correct guesses) and per-user
  history (date, words tried, correct guesses)
- Show/hide toggle on both password fields
- Session-based auth (no Spring Security dependency needed - just its BCrypt
  hashing utility) with role-based route protection for Player vs Admin

## Requirements
- Java 17+
- Maven 3.8+ (needs internet access the first time, to download dependencies
  from Maven Central)

## Run
```bash
mvn spring-boot:run
```
Then open **http://localhost:8081** in a browser.

Default admin account (seeded on first run): `admin` / `Admin$123`.
Players register from the UI. Data is stored in a file-based H2 database at
`./data/gamedb.mv.db`, so it survives restarts (this folder is git-ignored).

## Test
```bash
mvn test
```
Tests run against an isolated in-memory database (see `src/test/resources/application.properties`)
and never touch your real `./data/gamedb` file.

## Viewing the database
Two options:
- **H2 console (GUI, no extra tools needed):** with the app running, open
  **http://localhost:8080/h2-console**. JDBC URL: `jdbc:h2:file:./data/gamedb`,
  username `sa`, blank password. This is left open for local development
  convenience — if you ever deploy this publicly, disable it
  (`spring.h2.console.enabled=false`) or put it behind auth.
- **A desktop DB browser:** point any H2-compatible client (or DBeaver, etc.) at
  the same `./data/gamedb.mv.db` file.

## Project layout
```
src/main/java/com/opentext/guesstheword/
  model/        JPA entities: AppUser, Word, Game, Guess (+ Role, GameStatus enums)
  repository/   Spring Data JPA repositories
  service/      ScoreUtil (letter scoring), ValidationUtil (input rules),
                DictionaryService (word list), GameService, AuthService
  config/       DataSeeder (20 words + admin), AuthInterceptor, WebConfig
  controller/   AuthController, HomeController, GameController (JSON API), AdminController
  dto/          JSON response records (snake_case keys, matching static/game.js)
src/main/resources/
  templates/    Thymeleaf pages (login, register, play, admin)
  static/       style.css, game.js - shared, byte-for-byte, with the Python version's UI
  data/         valid_words.txt - the same dictionary used by the Python version
src/test/java/  Unit tests (scoring, validation) + a MockMvc integration test suite
```
