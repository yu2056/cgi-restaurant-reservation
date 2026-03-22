# Smart Restaurant Reservation System

A Spring Boot + Java restaurant reservation MVP for the CGI summer internship task.

## What is included

- visual floor plan with occupied, free, and recommended tables
- search by date, time, party size, duration, zone, and preferences
- recommendation scoring based on fit and preferences
- adjacent table combination for larger groups
- random seed reservations for demo data
- simple admin mode to drag and move tables on the floor plan
- optional best-effort meal suggestion from TheMealDB, with a local fallback
- REST API + unit tests
- Dockerfile

## Assumptions

- Default visit duration is 120 minutes.
- A reservation can use one table or two adjacent tables if that gives a better fit.
- If the selected external recipe API is unavailable, the app falls back to local menu suggestions.
- The floor plan is intentionally simple and uses absolute positioning.

## Run locally

### Requirements

- Java 21+
- Maven 3.9+ recommended

### Start

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

### Build and test

```bash
mvn test
mvn package
```

## API

- `GET /api/plan?date=YYYY-MM-DD&time=HH:MM&partySize=2&durationMinutes=120`
- `POST /api/reservations`
- `GET /api/admin/tables`
- `PUT /api/admin/tables/{id}/position`

## Notes on implementation

The recommendation engine gives a higher score to:
- exact or near-exact seat fit
- matching preferences
- matching zone
- single tables over combined tables when the score is similar

Random seed reservations are created on startup so the floor plan is not empty.

## Time spent

12 days of constant work with exploring of new elements what made this work challenging.

## Difficulties

It was first time with some elements of this project to work with and here was a lot of new to me.

## AI assistance

This project was partly generated and refined with AI assistance.