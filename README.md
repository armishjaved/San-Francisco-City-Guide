# SF Explorer: A San Francisco City Guide

A Java desktop app for exploring San Francisco. Browse restaurants, landmarks, events, and parks using live data from the city's open data portal, sign in with a secure account, and save favorites to a local database.

## Features

- **Four categories:** Eat & Drink, Attractions, Events, and Outdoor, each in its own tab.
- **Live city data:** pulls places from **DataSF**, San Francisco's open data API. If the network is unavailable, it falls back to bundled sample data so the app always runs.
- **Responsive UI:** data loads on background threads, so the interface never freezes while requests are in flight.
- **Accounts and favorites:** users sign in, save favorite places, and check in. Everything is stored in a local SQLite database.
- **Secure passwords:** passwords are never stored in plain text (see Security below).

## How it's built

| Area | What's used |
|---|---|
| UI | Java Swing |
| Networking | Java `HttpClient` (Java 11+) calling DataSF's Socrata API |
| JSON | Jackson |
| Database | SQLite through JDBC |
| Concurrency | `ExecutorService` thread pool for API calls; background threads for database work |
| Security | PBKDF2 password hashing with salt and pepper |
| Build | Maven, Java 17 |

### Concurrency
- API requests run on a **fixed thread pool** (`ExecutorService`), so switching tabs and loading data doesn't block the Swing UI thread.
- Database writes run on background threads and **retry with backoff** if SQLite is briefly locked.

### Security
- Passwords are hashed with **PBKDF2 (HMAC-SHA256)** at **120,000 iterations**.
- Each password gets a random **16-byte salt**, plus an app-wide **pepper**.
- Logins are checked with a **constant-time comparison** to avoid timing attacks.

### Data
- Live endpoints (DataSF): restaurants, landmarks, events, and Rec & Park facilities.
- Offline fallback: JSON samples in `src/main/resources/sample-data/`.
- The database is created automatically at `~/.sfexplorer/sfexplorer.db`, with tables for `users`, `favorites`, and `checkins`.

## Project structure

```
src/main/java/com/sfexplorer/
  App.java                  Entry point
  ui/                       Swing screens: main window, category tabs, favorites, login
  service/                  DataSF client (HttpClient) and place loading with offline fallback
  db/                       SQLite setup and repositories for users, favorites, check-ins
  security/PasswordHasher   PBKDF2 hashing with salt and pepper
  model/                    Place and Category
  util/                     JSON-to-Place mapping
```

## Run it

You need **Java 17+** and **Maven**.

```bash
git clone https://github.com/armishjaved/San-Francisco-City-Guide.git
cd San-Francisco-City-Guide
mvn compile exec:java -Dexec.mainClass=com.sfexplorer.App
```

Create an account in the login window, pick a category, and start saving favorites.

## What I'd improve next

- Add a search bar across all categories
- Cache API responses so repeat visits load instantly
- Move the pepper out of the source code into an environment variable
- Add unit tests for the password hasher and repositories
