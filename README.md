# 🔗 URL Shortener API

A production-ready, full-stack **URL Shortening Service** built with **Spring Boot 4.0**, featuring JWT authentication, rate limiting, click analytics, Redis caching, and comprehensive API documentation.

**🌐 Live Demo:** [https://url-shortener-zcrg.onrender.com](https://url-shortener-zcrg.onrender.com)

**📚 API Documentation:** [Swagger UI](https://url-shortener-zcrg.onrender.com/swagger-ui.html)

---

## ✨ Features

### Core Features
- 🔗 **URL Shortening** — Convert long URLs into short, shareable links
- 🎯 **Custom Aliases** — Choose your own short codes (e.g., `my-link`)
- ⏰ **Expiration Support** — Set expiry dates for temporary links
- 🔄 **Instant Redirect** — Fast 302 redirects to original URLs

### Security
- 🔐 **JWT Authentication** — Secure token-based authentication
- 🛡️ **Spring Security** — Protected endpoints for authenticated users only
- 🚦 **Rate Limiting** — Max 10 requests per minute per IP (Redis-backed)
- 🔒 **Password Encryption** — BCrypt hashing for secure password storage

### Analytics
- 📊 **Click Tracking** — Track every click with detailed metadata
- 🌍 **Geographic Data** — IP-based location tracking
- 📱 **Device Analytics** — Browser, OS, and device type detection
- 📈 **Analytics Dashboard** — View statistics per short URL

### Performance & Scalability
- ⚡ **Redis Caching** — Fast URL lookups with configurable TTL
- 🗄️ **PostgreSQL** — Reliable persistent storage
- 🐳 **Docker Ready** — Multi-stage Dockerfile for optimized builds
- ☁️ **Cloud Deployed** — Production deployment on Render

### Developer Experience
- 📖 **Swagger/OpenAPI** — Interactive API documentation
- ✅ **70%+ Test Coverage** — Unit & integration tests with JaCoCo
- 🧪 **H2 Test Database** — Isolated test environment

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client                                   │
│                    (Browser / Mobile / API)                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Filter                        │
│              (JWT Authentication + Rate Limiting)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REST Controllers                            │
│     ┌─────────────┐  ┌──────────────┐  ┌───────────────┐        │
│     │ AuthController│ │UrlController │ │HealthController│        │
│     └─────────────┘  └──────────────┘  └───────────────┘        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Services                                  │
│  ┌───────────┐  ┌──────────┐  ┌────────────────┐  ┌───────────┐ │
│  │AuthService│  │UrlService│  │AnalyticsService│  │CacheService│ │
│  └───────────┘  └──────────┘  └────────────────┘  └───────────┘ │
└───────────────────────────┬─────────────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
┌──────────────────────┐      ┌──────────────────────┐
│      PostgreSQL      │      │        Redis         │
│   (Persistent Data)  │      │   (Cache + Rate Limit)│
└──────────────────────┘      └──────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 4.0.4 |
| **Language** | Java 17+ |
| **Security** | Spring Security + JWT (jjwt 0.12.6) |
| **Database** | PostgreSQL |
| **Caching** | Redis (Lettuce) |
| **ORM** | Spring Data JPA / Hibernate |
| **Validation** | Jakarta Bean Validation |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |
| **Testing** | JUnit 5, Mockito, MockMvc, H2 |
| **Code Coverage** | JaCoCo |
| **Build Tool** | Maven |
| **Containerization** | Docker |
| **Deployment** | Render |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Redis 6+**

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/urlshortner.git
   cd urlshortner
   ```

2. **Set up PostgreSQL**
   ```bash
   # Create database
   createdb urlshortener
   ```

3. **Start Redis**
   ```bash
   redis-server
   ```

4. **Configure environment variables** (or use defaults in application.properties)
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/urlshortener
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   export JWT_SECRET=your-super-secret-key-at-least-256-bits-long
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **Access the API**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Docker Setup

```bash
# Build the image
docker build -t urlshortener .

# Run with Docker Compose (if you have docker-compose.yml)
docker-compose up -d
```

---

## 📡 API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Create a new account |
| `POST` | `/api/auth/login` | Login and get JWT token |

### URL Operations

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `POST` | `/api/shorten` | ✅ Yes + Rate Limited | Create a short URL |
| `GET` | `/{shortCode}` | ❌ No | Redirect to original URL |
| `GET` | `/api/analytics/{shortCode}` | ❌ No | Get click analytics |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/health` | Service health status |

---

## 📘 Usage Examples

### 1. Register a New User

```bash
curl -X POST https://url-shortener-zcrg.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "email": "user@example.com",
  "message": "Registration successful"
}
```

### 2. Login

```bash
curl -X POST https://url-shortener-zcrg.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePassword123"
  }'
```

### 3. Shorten a URL (Authenticated)

```bash
curl -X POST https://url-shortener-zcrg.onrender.com/api/shorten \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "originalUrl": "https://www.example.com/very/long/url/that/needs/shortening",
    "customAlias": "my-link",
    "expiryInDays": 30
  }'
```

**Response:**
```json
{
  "shortUrl": "https://url-shortener-zcrg.onrender.com/my-link",
  "shortCode": "my-link",
  "originalUrl": "https://www.example.com/very/long/url/that/needs/shortening",
  "createdAt": "2026-03-26T10:30:00",
  "expiresAt": "2026-04-25T10:30:00"
}
```

### 4. Access Short URL (Public)

Simply visit: `https://url-shortener-zcrg.onrender.com/my-link`

You'll be redirected to the original URL automatically.

### 5. View Analytics

```bash
curl https://url-shortener-zcrg.onrender.com/api/analytics/my-link
```

**Response:**
```json
{
  "shortCode": "my-link",
  "totalClicks": 150,
  "clicksLast7Days": 45,
  "byCountry": [
    {"country": "US", "count": 80},
    {"country": "IN", "count": 40}
  ],
  "byDevice": [
    {"device": "DESKTOP", "count": 100},
    {"device": "MOBILE", "count": 50}
  ],
  "byBrowser": [
    {"browser": "Chrome", "count": 90},
    {"browser": "Firefox", "count": 30}
  ]
}
```

---

## 🔐 Authentication Flow

```
┌─────────┐          ┌──────────────┐          ┌─────────────┐
│  Client │          │ Auth Service │          │  Database   │
└────┬────┘          └──────┬───────┘          └──────┬──────┘
     │                      │                         │
     │ POST /api/auth/login │                         │
     │ {email, password}    │                         │
     │─────────────────────>│                         │
     │                      │                         │
     │                      │ Find user by email      │
     │                      │────────────────────────>│
     │                      │                         │
     │                      │ User record             │
     │                      │<────────────────────────│
     │                      │                         │
     │                      │ Verify BCrypt password  │
     │                      │ Generate JWT token      │
     │                      │                         │
     │ {token: "eyJ..."}    │                         │
     │<─────────────────────│                         │
     │                      │                         │
     │ POST /api/shorten    │                         │
     │ Authorization: Bearer eyJ...                   │
     │────────────────────────────────────────────────│
     │                      │                         │
```

---

## 🚦 Rate Limiting

The API implements rate limiting to prevent abuse:

- **Limit:** 10 requests per minute per IP address
- **Scope:** `/api/shorten` endpoint only
- **Storage:** Redis-backed counters
- **Response Header:** `X-RateLimit-Remaining` shows remaining requests

When limit is exceeded:
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 60 seconds."
}
```

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Generate Coverage Report

```bash
./mvnw test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

### Test Categories

| Type | Location | Description |
|------|----------|-------------|
| **Unit Tests** | `src/test/java/.../Service/` | Test services in isolation with Mockito |
| **Integration Tests** | `src/test/java/.../Controller/` | Test full request lifecycle with MockMvc |
| **Repository Tests** | `src/test/java/.../Repository/` | Test JPA queries with H2 database |
| **Security Tests** | `src/test/java/.../Security/` | Test JWT utilities and rate limiting |

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/urlshortener` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `root123` |
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | Dev default (change in production!) |
| `JWT_EXPIRATION_MS` | Token validity in milliseconds | `86400000` (24 hours) |
| `REDIS_HOST` | Redis server host | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `REDIS_PASSWORD` | Redis password | (empty) |
| `BASE_URL` | Base URL for generated short links | `http://localhost:8080` |
| `RATE_LIMIT_MAX` | Max requests per window | `10` |
| `RATE_LIMIT_WINDOW` | Rate limit window in seconds | `60` |
| `CACHE_TTL_HOURS` | Cache TTL for URL lookups | `24` |
| `LOG_LEVEL` | Application log level | `INFO` |

---

## 📁 Project Structure

```
src/main/java/com/kushagra/urlshortner/
├── Config/
│   ├── SecurityConfig.java       # Spring Security configuration
│   ├── AsyncConfig.java          # Async executor for analytics
│   ├── RedisConfig.java          # Redis connection configuration
│   └── OpenApiConfig.java        # Swagger/OpenAPI configuration
├── Controller/
│   ├── AuthController.java       # Login/Register endpoints
│   ├── UrlController.java        # URL shortening & redirect
│   └── HealthController.java     # Health check endpoint
├── DTO/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── ShortenRequest.java
│   ├── ShortenResponse.java
│   └── AnalyticsSummary.java
├── Entity/
│   ├── User.java                 # User entity for authentication
│   ├── Url.java                  # URL mapping entity
│   └── ClickEvent.java           # Click analytics entity
├── Exception/
│   ├── GlobalExceptionHandler.java
│   ├── UrlNotFoundException.java
│   ├── UrlExpiredException.java
│   ├── AliasAlreadyExistsException.java
│   └── RateLimitExceededException.java
├── Repository/
│   ├── UserRepository.java
│   ├── UrlRepository.java
│   └── ClickEventRepository.java
├── Security/
│   ├── JwtUtils.java             # JWT token generation/validation
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── RateLimitingService.java
├── Service/
│   ├── AuthService.java          # Authentication business logic
│   ├── UrlService.java           # URL shortening logic
│   ├── AnalyticsService.java     # Click tracking & analytics
│   ├── CacheService.java         # Redis caching operations
│   ├── ShortCodeGenerator.java   # Base62 encoding
│   └── UserAgentParser.java      # Browser/device detection
└── UrlshortnerApplication.java   # Main application class
```

---

## ☁️ Deployment

### Deploy to Render

1. **Push to GitHub**
   ```bash
   git add .
   git commit -m "Ready for deployment"
   git push origin main
   ```

2. **Create services on Render:**
   - **Web Service** (Docker) — Your Spring Boot app
   - **PostgreSQL** — Managed database
   - **Redis** — Key-value store for caching & rate limiting

3. **Configure environment variables** in Render dashboard

4. **Deploy** — Render auto-deploys on git push

### render.yaml (Blueprint)

The repository includes a `render.yaml` for one-click deployment.

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

**Kushagra**

- GitHub: [@kushagra](https://github.com/kushagra)
- Live Demo: [url-shortener-zcrg.onrender.com](https://url-shortener-zcrg.onrender.com)

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [JWT.io](https://jwt.io/)
- [Render](https://render.com/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

Made with ❤️ and ☕

