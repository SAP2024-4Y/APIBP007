# APIBP007 — DineIndia Restaurant Reservation API

Course: SESAPZG504 - API Based Products | Section: 2024 4Y | Instructor: Vinaya Sathyanarayanan

## Team Details

| # | Name | Roll No | GitHub ID | Email |
|---|---|---|---|---|
| 1 | Aishwarya Santhosh | 2024SL70015 | [Aicodes224](https://github.com/Aicodes224) | 2024sl70015@wilp.bits-pilani.ac.in |
| 2 | Lakshmi Priya | 2024SL70021 | [Laksh-mi-Priya](https://github.com/Laksh-mi-Priya) | 2024sl70021@wilp.bits-pilani.ac.in |
| 3 | Veena Gishu | 2024SL70044 | [VeenaGishu](https://github.com/VeenaGishu) | 2024sl70044@wilp.bits-pilani.ac.in |
| 4 | Meghna Rajesh | 2024SL70012 | [meghnarajesh](https://github.com/meghnarajesh) | 2024sl70012@wilp.bits-pilani.ac.in |
| 5 | Sinchana V | 2024SL70034 | [sinchanapoojary-blip](https://github.com/sinchanapoojary-blip) | 2024sl70034@wilp.bits-pilani.ac.in |

- Team ID: `APIBP007`
- Chosen Approach: `A` — REST-First + GraphQL Layer

## Project Overview

DineIndia is a restaurant reservation platform that provides both
REST and GraphQL APIs using a shared database and business logic.

The application allows customers to discover restaurants, filter
and sort listings, search by geographical location, check dining
slot availability, book tables and manage reservations.

Administrators can create, update and delete restaurants and
manage dining slots through JWT-protected APIs.

## Key Features

- 50 seeded restaurants across five Indian cities.
- REST and GraphQL APIs.
- Cursor-based pagination.
- Restaurant filtering and sorting.
- Radius-based and bounding-box geographical search.
- GeoJSON location coordinates.
- Calendar-based dining slot availability.
- Restaurant and dining slot management.
- Customer booking and cancellation.
- JWT authentication and role-based authorization.
- Double-booking prevention.
- Structured error handling.
- API rate limiting.
- Swagger API documentation.
- Interactive GraphQL IDE.
- Docker-based deployment.
- A browser interface for restaurant discovery and reservations.

## Technology Stack

| Component | Technology |
|---|---|
| Backend | Python, FastAPI |
| REST API | FastAPI |
| GraphQL API | GraphQL Core |
| Database | SQLite |
| Frontend | HTML, CSS, JavaScript |
| Authentication | JSON Web Token (JWT) |
| REST Documentation | OpenAPI, Swagger UI |
| GraphQL IDE | GraphiQL |
| Testing | Pytest |
| Deployment | Docker, Docker Compose |

## Quick start

```bash
docker compose up

# Server:      http://localhost:8080
# Health:      http://localhost:8080/health
# REST docs:   http://localhost:8080/docs
# GraphQL IDE: http://localhost:8080/graphql
```

Run the command from the root directory of the APIBP007 repository.

Open [http://localhost:8080](http://localhost:8080) for the DineIndia
browser interface. Use the filters to find restaurants, sign in with the
demo customer credentials, and view available dining times.

## Demo credentials

### Customer

POST /api/v1/auth/token

```json
{
  "username": "demo",
  "password": "demo-password"
}
```

### Admin

POST /api/v1/auth/token

```json
{
  "username": "admin",
  "password": "admin-password"
}
```

## Authentication

The application uses JWT-based authentication and role-based
authorization.

Generate a token using the authentication endpoint:

```bash
curl -X POST "http://localhost:8080/api/v1/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}'
```

Copy the token returned in the response and include it in the
Authorization header when accessing protected endpoints.

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## REST API Endpoints

| Method | Endpoint | Access |
|---|---|---|
| GET | /health | Public |
| POST | /api/v1/auth/token | Public |
| GET | /api/v1/restaurants | Public |
| GET | /api/v1/restaurants/{id} | Public |
| GET | /api/v1/restaurants/map | Public |
| GET | /api/v1/calendar | Public |
| POST | /api/v1/restaurants | Admin |
| PATCH | /api/v1/restaurants/{id} | Admin |
| DELETE | /api/v1/restaurants/{id} | Admin |
| POST | /api/v1/slots | Admin |
| POST | /api/v1/bookings | Customer |
| GET | /api/v1/bookings | Customer |
| DELETE | /api/v1/bookings/{id} | Customer |

## GraphQL Operations

All GraphQL queries and mutations are available through:

POST /graphql

### Queries

| Query | Access |
|---|---|
| restaurants | Public |
| restaurant | Public |
| map | Public |
| calendar | Public |
| bookings | Customer |

### Mutations

| Mutation | Access |
|---|---|
| login | Public |
| addRestaurant | Admin |
| updateRestaurant | Admin |
| deleteRestaurant | Admin |
| addSlot | Admin |
| book | Customer |
| cancelBooking | Customer |

## Compare response sizes

```bash
docker compose exec app python tests/measure.py
```

Run this while the app container is active. The script compares the
response body sizes for sample REST and GraphQL requests; it is a small
measurement utility, not an automated test suite.

## Sample REST call

Retrieve restaurants using filtering, sorting and cursor-based
pagination:

```bash
curl "http://localhost:8080/api/v1/restaurants?limit=10&status=OPEN&sort=id&order=asc"
```

To retrieve the next page, use the cursor returned in the previous
response as the `after` query parameter.

## Sample GraphQL query

```graphql
query {
  restaurants(
    first: 10
    status: "OPEN"
    sort: "id"
    order: "asc"
  ) {
    edges {
      cursor
      node {
        id
        name
        city
        category
        status
        price
        latitude
        longitude
        location {
          type
          coordinates
        }
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
```

## Sample authenticated REST call

Book an available dining slot using a customer JWT token:

```bash
curl -X POST "http://localhost:8080/api/v1/bookings" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"slot_id\":$SLOT_ID}"
```

Set TOKEN to the JWT returned by the authentication endpoint and
SLOT_ID to an available slot ID returned by the calendar API.

## Error Codes

| HTTP Status | Error Code |
|---|---|
| 400 | BAD_INPUT |
| 400 | INVALID_CURSOR |
| 401 | UNAUTHENTICATED |
| 403 | FORBIDDEN |
| 404 | NOT_FOUND |
| 409 | CONFLICT |
| 422 | INVALID_WINDOW |
| 422 | INVALID_LOCATION |
| 422 | INVALID_PRICE |
| 429 | RATE_LIMITED |
| 500 | INTERNAL_ERROR |

REST APIs return the corresponding HTTP status and structured
error response.

GraphQL APIs return structured domain errors through
errors[].extensions.code.

## Project Structure

```text
APIBP007/
├── docs/
│   ├── openapi.yaml
│   ├── schema.graphql
│   └── ADR.md
├── src/
│   ├── main.py
│   └── index.html
├── tests/
│   └── measure.py
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
├── team.json
└── README.md
```

## Documentation

- [REST API Specification](docs/openapi.yaml)
- [GraphQL Schema](docs/schema.graphql)
- [Architecture Decision Record](docs/ADR.md)
