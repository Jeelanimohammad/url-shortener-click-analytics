# 🔗 URL Shortener with Click Analytics

A **production-quality Spring Boot backend** that converts long URLs into short links and tracks every click with full analytics. Built as a portfolio project demonstrating Java, Spring Boot, REST API design, PostgreSQL, JPA/Hibernate, input validation, exception handling, and unit testing.

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Database Design](#database-design)
- [Prerequisites](#prerequisites)
- [Setup & Run](#setup--run)
- [API Reference](#api-reference)
- [Postman Examples](#postman-examples)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Interview Q&A](#interview-qa)

---

## ✨ Features

| Feature | Details |
|---|---|
| **URL Shortening** | Generates a unique 7-character alphanumeric short code using `SecureRandom` |
| **Deduplication** | Returns the existing short link if the same URL is submitted again |
| **Collision Handling** | Retries generation if a code already exists (up to 10 attempts) |
| **URL Redirection** | `GET /{shortCode}` → HTTP 302 redirect to the original URL |
| **Click Tracking** | Increments a counter and stores a timestamped row per visit |
| **Analytics API** | Returns total clicks and full click-timestamp history |
| **URL Management** | Full CRUD: create, read (one + all), delete |
| **Input Validation** | Rejects blank inputs, malformed URLs, and unsafe schemes (ftp, javascript) |
| **Error Handling** | Uniform JSON error responses via `@RestControllerAdvice` |
| **Unit Tests** | 20+ tests with JUnit 5 + Mockito — no database required |

---

## 🏛️ Architecture

```
┌─────────────┐   HTTP    ┌────────────────┐   calls   ┌──────────────────┐
│   Client    │ ────────► │   Controller   │ ────────► │    Service       │
│ (Browser /  │           │  UrlController │           │ UrlShortenerSvc  │
│  Postman)   │           │  RedirectCtrl  │           │ CodeGeneratorSvc │
└─────────────┘           └────────────────┘           │ ValidationSvc    │
                                                        └────────┬─────────┘
                                                                 │ uses
                                                        ┌────────▼─────────┐
                                                        │   Repository     │
                                                        │ ShortUrlRepo     │
                                                        │ ClickEventRepo   │
                                                        └────────┬─────────┘
                                                                 │ JPA/Hibernate
                                                        ┌────────▼─────────┐
                                                        │   PostgreSQL     │
                                                        │  short_urls      │
                                                        │  click_events    │
                                                        └──────────────────┘
```

**Package responsibilities:**

| Package | Responsibility |
|---|---|
| `controller` | Parse HTTP requests, call service, return HTTP responses |
| `service` | All business logic: validation, generation, tracking, analytics |
| `repository` | Spring Data JPA interfaces — SQL is auto-generated |
| `entity` | JPA entity classes that map to database tables |
| `dto` | Request/response objects — never expose entities to the API |
| `exception` | Custom exceptions + global `@RestControllerAdvice` handler |
| `config` | Externalised configuration (base URL, etc.) |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Build | Maven | 3.9+ |
| Database | PostgreSQL | 15+ |
| ORM | Spring Data JPA / Hibernate | (included in Boot) |
| Validation | Jakarta Bean Validation | (included in Boot) |
| Testing | JUnit 5 + Mockito | (included in Boot) |

---

## 🗄️ Database Design

### `short_urls` table

| Column | Type | Constraints |
|---|---|---|
| `id` | BIGSERIAL | PRIMARY KEY |
| `original_url` | VARCHAR(2048) | NOT NULL |
| `short_code` | VARCHAR(10) | NOT NULL, UNIQUE |
| `created_at` | TIMESTAMP | NOT NULL |
| `click_count` | BIGINT | NOT NULL, DEFAULT 0 |

### `click_events` table

| Column | Type | Constraints |
|---|---|---|
| `id` | BIGSERIAL | PRIMARY KEY |
| `short_url_id` | BIGINT | FK → short_urls(id), NOT NULL |
| `clicked_at` | TIMESTAMP | NOT NULL |

**Relationship:** `short_urls` 1 ──── ∞ `click_events` (one URL has many click events).

> Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

---

## ✅ Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **PostgreSQL 15+** running locally
- **Git** — `git --version`

---

## 🚀 Setup & Run

### 1 — Clone the repository

```bash
git clone https://github.com/Jeelanimohammad/url-shortener-click-analytics.git
cd url-shortener-click-analytics
```

### 2 — Create the PostgreSQL database

```sql
-- Run in psql or pgAdmin
CREATE DATABASE url_shortener_db;
```

### 3 — Configure credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=your_pg_username
spring.datasource.password=your_pg_password
```

**Or** export environment variables (recommended):

```bash
# Windows PowerShell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

### 4 — Build and run

```bash
# Add Maven to PATH if needed
# Windows: $env:PATH += ";C:\Users\JEELANI\maven\apache-maven-3.9.6\bin"

mvn clean package -DskipTests
java -jar target/url-shortener-1.0.0.jar
```

Or run directly:

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

---

## 📡 API Reference

### Create a short URL

```
POST /api/urls
Content-Type: application/json
```

**Request:**
```json
{ "originalUrl": "https://www.example.com/some/very/long/path?param=value" }
```

**Response 201 Created:**
```json
{
  "id": 1,
  "originalUrl": "https://www.example.com/some/very/long/path?param=value",
  "shortCode": "aB3xK9p",
  "shortUrl": "http://localhost:8080/aB3xK9p",
  "createdAt": "2024-05-01T10:30:00",
  "clickCount": 0
}
```

**Error 400 — invalid URL:**
```json
{ "status": 400, "error": "Bad Request", "message": "URL scheme 'ftp' is not allowed.", "timestamp": "..." }
```

---

### Get URL details

```
GET /api/urls/{shortCode}
```

**Response 200 OK:** same as create response  
**Response 404:** `{ "status": 404, "error": "Not Found", "message": "Short code 'xyz' not found" }`

---

### List all URLs

```
GET /api/urls
```

**Response 200 OK:** JSON array of UrlResponse objects.

---

### Delete a URL

```
DELETE /api/urls/{shortCode}
```

**Response 204 No Content** (success)  
**Response 404** if code not found.

---

### Redirect (browser-compatible)

```
GET /{shortCode}
```

**Response 302 Found** → `Location: <original URL>`  
**Response 404** if code not found (click NOT recorded).

> Paste `http://localhost:8080/aB3xK9p` directly in a browser to test redirection.

---

### Get analytics

```
GET /api/urls/{shortCode}/analytics
```

**Response 200 OK:**
```json
{
  "shortCode": "aB3xK9p",
  "originalUrl": "https://www.example.com/...",
  "shortUrl": "http://localhost:8080/aB3xK9p",
  "createdAt": "2024-05-01T10:30:00",
  "totalClicks": 3,
  "clickHistory": [
    "2024-05-01T11:00:00",
    "2024-05-01T14:30:00",
    "2024-05-02T09:15:00"
  ]
}
```

---

## 🧪 Postman Examples

### Successful flow

1. **Create:** `POST /api/urls` → copy `shortCode` from response.
2. **Redirect:** Open browser → `http://localhost:8080/{shortCode}` → confirms redirect.
3. **Analytics:** `GET /api/urls/{shortCode}/analytics` → `totalClicks` should be 1.

### Error scenarios

| Scenario | Request | Expected |
|---|---|---|
| Blank URL | `POST /api/urls` `{ "originalUrl": "" }` | 400 Validation Failed |
| Bad scheme | `POST /api/urls` `{ "originalUrl": "ftp://files.com" }` | 400 Bad Request |
| Unknown code | `GET /api/urls/xxxxxxx` | 404 Not Found |
| Delete then access | `DELETE /api/urls/{code}` then `GET /{code}` | 404 Not Found |

---

## 🧑‍🔬 Running Tests

```bash
mvn test
```

Test coverage:
- `UrlShortenerServiceTest` — 10 tests covering create, deduplicate, get, list, delete, resolve, and analytics
- `ShortCodeGeneratorServiceTest` — 5 tests covering length, charset, collision, and safety valve
- `UrlValidationServiceTest` — 7 tests covering valid URLs, bad schemes, and malformed strings

---

## 📁 Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── UrlShortenerApplication.java   ← Entry point
│   │   │   ├── config/AppConfig.java           ← Base URL config
│   │   │   ├── controller/
│   │   │   │   ├── UrlController.java          ← /api/urls endpoints
│   │   │   │   └── RedirectController.java     ← /{shortCode} redirect
│   │   │   ├── service/
│   │   │   │   ├── UrlShortenerService.java    ← Core business logic
│   │   │   │   ├── ShortCodeGeneratorService.java
│   │   │   │   └── UrlValidationService.java
│   │   │   ├── repository/
│   │   │   │   ├── ShortUrlRepository.java
│   │   │   │   └── ClickEventRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── ShortUrl.java
│   │   │   │   └── ClickEvent.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateUrlRequest.java
│   │   │   │   ├── UrlResponse.java
│   │   │   │   ├── AnalyticsResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   └── exception/
│   │   │       ├── ShortCodeNotFoundException.java
│   │   │       ├── InvalidUrlException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/urlshortener/service/
│       ├── UrlShortenerServiceTest.java
│       ├── ShortCodeGeneratorServiceTest.java
│       └── UrlValidationServiceTest.java
├── pom.xml
├── .gitignore
└── README.md
```

---

## 💼 Interview Q&A

**Q: Why did you use `SecureRandom` instead of `Random`?**  
A: `SecureRandom` uses a cryptographically strong RNG seeded from OS entropy, making codes harder to predict. `java.util.Random` is predictable — someone could potentially guess the next code.

**Q: How do you handle short-code collisions?**  
A: The service calls `repository.existsByShortCode()` before returning a code. If it exists, the generator retries. With 62^7 ≈ 3.5 trillion possibilities, collisions are astronomically rare, but we handle them gracefully with a safety valve at 10 attempts.

**Q: Why HTTP 302 instead of 301 for redirects?**  
A: 301 (Moved Permanently) is cached by browsers forever. Once cached, the browser never asks our server again — breaking click analytics. 302 (Found) tells the browser to re-check each time, ensuring every visit is tracked.

**Q: Why use DTOs instead of exposing JPA entities directly?**  
A: Entities contain JPA metadata and can trigger lazy-loading exceptions when serialized. DTOs carry only the fields we want to expose, decouple the API contract from the database schema, and make validation cleaner.

**Q: What is `@Transactional` and why is it on `resolveAndTrack`?**  
A: `@Transactional` wraps the method in a single database transaction. If we increment the click count but then fail to insert the `ClickEvent`, the transaction rolls back — keeping the count and the history consistent.

**Q: What is the difference between unit tests and integration tests?**  
A: Unit tests (like ours) mock all dependencies and test one class in isolation — fast and focused. Integration tests spin up the full Spring context and a real database to verify that all layers work together correctly — slower but catches more realistic bugs.

**Q: How would you scale this for millions of URLs?**  
A: Add a Redis cache layer for hot short codes, use a read replica for analytics queries, consider a distributed ID generator (Snowflake) instead of random codes, and deploy behind a CDN for the redirect endpoint.

---

## 🔒 Security Notes

- Database credentials are read from environment variables — never hardcoded.
- Only `http` and `https` URL schemes are accepted — prevents `javascript:` XSS attacks.
- No `.env` or `application-local.properties` files are committed (covered by `.gitignore`).

---

*Built by [Jeelanimohammad](https://github.com/Jeelanimohammad) · Spring Boot URL Shortener with Click Analytics*
