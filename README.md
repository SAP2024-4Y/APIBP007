# APIBP001 — Table4Us: Group Dining Recommendation Platform
 
Course: SESAPZG504 - API Based Products | Section: 2024 4Y | Instructor: Vinaya Sathyanarayanan
 
## Team Details
 
| # | Name | Roll No | GitHub ID | Email |
|---|---|---|---|---|
| 1 | Aishwarya Santhosh | 2024SL70015 | [Aicodes224](https://github.com/Aicodes224) | [2024sl70015@wilp.bits-pilani.ac.in](mailto:2024sl70015@wilp.bits-pilani.ac.in) |
| 2 | Lakshmi Priya | 2024SL70021 | [Laksh-mi-Priya](https://github.com/Laksh-mi-Priya) | [2024sl70021@wilp.bits-pilani.ac.in](mailto:2024sl70021@wilp.bits-pilani.ac.in) |
| 3 | Veena Gishu | 2024SL70044 | [VeenaGishu](https://github.com/VeenaGishu) | [2024sl70044@wilp.bits-pilani.ac.in](mailto:2024sl70044@wilp.bits-pilani.ac.in) |
| 4 | Meghna Rajesh | 2024SL70012 | [meghnarajesh](https://github.com/meghnarajesh) | [2024sl70012@wilp.bits-pilani.ac.in](mailto:2024sl70012@wilp.bits-pilani.ac.in) |
| 5 | Sinchana V | 2024SL70034 | [sinchanapoojary-blip](https://github.com/sinchanapoojary-blip) | [2024sl70034@wilp.bits-pilani.ac.in](mailto:2024sl70034@wilp.bits-pilani.ac.in) |
 
- Team ID: `APIBP007`
- Chosen Approach: `A` — REST-First + GraphQL Layer
 
## Quick start
 
### Prerequisites
 
- Docker Engine and Docker Compose
- Git
 
### Clone the repository
 
```bash
git clone https://github.com/SAP2024-4Y/APIBP007.git
cd APIBP007
```
 
### Run the application
 
```bash
docker compose up
```
 
The application is expected to expose these endpoints when the frontend, backend and Docker services are configured:
 
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Health check: `http://localhost:8080/health`
- REST API documentation: `http://localhost:8080/docs`
- GraphQL endpoint / IDE: `http://localhost:8080/graphql`
 
The first startup may take longer while Docker builds the application, PostgreSQL starts, database migrations run and demonstration data is loaded.
 
To stop the application:
 
```bash
docker compose down
```
 
To stop the application and remove its database volume:
 
```bash
docker compose down -v
```
 
**Warning:** `docker compose down -v` deletes the local database data.
 
> **Development note:** Docker and Docker Compose setup will be completed during the DevOps phase. Until then, run the backend and frontend separately using the instructions maintained in their respective project folders.
 
## Demo credentials
 
The local demonstration account is expected to use the following credentials:
 
```http
POST /api/v1/auth/token
Content-Type: application/json
 
{
  "username": "demo",
  "password": "demo-password"
}
```
 
Use the returned access token as a Bearer token for protected endpoints. These credentials are intended for the local demonstration environment only.
 
## Run tests
 
### Backend tests
 
```bash
cd backend
mvn test
```
 
Run the full Maven verification lifecycle:
 
```bash
cd backend
mvn verify
```
 
### Frontend tests
 
```bash
cd frontend
npm install
npm test
```
 
The frontend test command depends on the test script configured in `frontend/package.json`. Update this section if the final project uses a different command.
 
## Sample REST call
 
The following example lists restaurants using the List API. Replace `<token>` with the access token returned by the login endpoint.
 
```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/restaurants?status=ACTIVE&category=NORTH_INDIAN&min_price=100&max_price=500&sort=rating&order=desc&limit=10%22
```
 
## About Table4Us
 
Table4Us is a Group Dining Recommendation Platform that helps people choose restaurants based on their shared dining preferences and combined budget.
 
Instead of making every person search for restaurants independently, a group can provide its requirements through a chatbot or guided interface. These requirements include preferred cuisine, total group budget, location, group size, dining date and time, and optional dietary preferences.
 
Table4Us evaluates restaurant options and presents recommendations with estimated total group cost, estimated per-person cost, distance, ratings, group-size suitability and reservation-slot information where supported. Users can compare restaurants and continue to an appropriate external restaurant, ordering or reservation platform through configured links.
 
Table4Us is a restaurant discovery and decision-support application. Redirecting a user to an external platform does not itself confirm a restaurant booking or place a food order.
 
## Core features
 
- **Conversational dining assistant:** Collects group dining requirements through chatbot messages or guided questions.
- **Group budget calculation:** Estimates the total group bill and per-person expenditure using available menu and pricing data.
- **Restaurant recommendations:** Ranks restaurants using cuisine, budget, location, ratings, group size and supported availability information.
- **Explainable recommendations:** Shows why a restaurant matches the group's requirements.
- **Restaurant and menu comparison:** Helps users compare menu items and prices across restaurants.
- **Location-based discovery:** Supports nearby restaurant search, radius filtering, geographical bounding-box search and map display.
- **Reservation availability:** Searches configured reservation slots and prevents conflicting reservations within Table4Us records.
- **External platform redirection:** Provides configured links to restaurant websites and supported food-ordering or reservation platforms.
 
## API overview
 
Table4Us follows **Approach A: REST-First + GraphQL Layer**. REST controllers and GraphQL resolvers use the same backend service layer so that business rules are not duplicated.
 
### REST API
 
The REST API is versioned under `/api/v1`.
 
| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/token` | Authenticate and obtain an access token |
| POST | `/api/v1/chat/sessions` | Create a chatbot session |
| POST | `/api/v1/chat/sessions/{id}/messages` | Submit a chatbot message |
| POST | `/api/v1/recommendations` | Generate group dining recommendations |
| GET | `/api/v1/restaurants` | List and filter restaurants |
| GET | `/api/v1/restaurants/{id}` | Retrieve restaurant details |
| GET | `/api/v1/restaurants/nearby` | Search restaurants within a radius |
| GET | `/api/v1/restaurants/bounds` | Search restaurants within map boundaries |
| GET | `/api/v1/food-items` | List standardized food items |
| GET | `/api/v1/food-items/{id}/compare` | Compare a food item across restaurants |
| GET | `/api/v1/slots` | Search reservation slots |
| POST | `/api/v1/reservations` | Create a reservation |
| GET | `/api/v1/reservations/{id}` | Retrieve reservation details |
 
The final endpoint catalogue and request/response contracts will be maintained in `docs/openapi.yaml`.
 
### GraphQL API
 
The GraphQL endpoint is:
 
```text
http://localhost:8080/graphql
```
 
GraphQL provides access to restaurant, menu, review, recommendation, location and reservation capabilities. The schema is maintained in `docs/schema.graphql`.
 
The GraphQL implementation supports queries and mutations, cursor pagination, DataLoader-based batching and schema evolution, including a deprecated field or argument where required by the assignment.
 
## Assignment domain patterns
 
### 1. List pattern
 
The restaurant List API supports:
 
- Filtering by restaurant status and category.
- Minimum and maximum price filtering.
- Sorting by supported fields and sort direction.
- Cursor/keyset pagination using `limit` and `after`.
- Pagination metadata, including whether more results are available.
 
Example:
 
```http
GET /api/v1/restaurants?status=ACTIVE&category=NORTH_INDIAN&min_price=100&max_price=500&sort=rating&order=desc&limit=10
```
 
### 2. Map pattern
 
The Map API supports:
 
- Radius search using latitude, longitude and `radius_km`.
- Bounding-box search using minimum and maximum latitude and longitude.
- Restaurant location display using Leaflet and OpenStreetMap.
 
Radius-search example:
 
```http
GET /api/v1/restaurants/nearby?latitude=12.9352&longitude=77.6245&radius_km=5
```
 
Bounding-box example:
 
```http
GET /api/v1/restaurants/bounds?min_lat=12.90&min_lon=77.60&max_lat=13.00&max_lon=77.70
```
 
GeoJSON point coordinates follow the `[longitude, latitude]` order.
 
### 3. Calendar pattern
 
The Calendar API supports:
 
- Searching reservation slots within an ISO 8601 date/time window.
- Checking configured restaurant slot capacity.
- Creating reservations.
- Detecting overlapping or conflicting reservations.
- Returning a conflict response when a requested slot is no longer available.
 
Example:
 
```http
GET /api/v1/slots?starts_at=2026-12-01T09:00:00Z&ends_at=2026-12-01T12:00:00Z
```
 
Availability based on local demonstration data is not represented as verified live availability from an external restaurant.
 
## Technology stack
 
| Layer | Technology |
|---|---|
| Frontend | React.js, Vite |
| Styling | Tailwind CSS |
| Backend | Java 21, Spring Boot |
| REST API | Spring Web |
| GraphQL API | Spring for GraphQL |
| Database | PostgreSQL |
| Persistence | Spring Data JPA / Hibernate |
| Authentication and authorization | Spring Security, JWT |
| Database migrations | Flyway |
| REST documentation | OpenAPI / Springdoc |
| Maps | Leaflet, OpenStreetMap |
| Build tool | Maven |
| Backend testing | JUnit 5, Mockito |
| Frontend testing | React Testing Library |
| Version control | Git, GitHub |
 
## Repository structure
 
```text
APIBP001/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/table4us/
│   │   │   │   ├── controller/
│   │   │   │   ├── graphql/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── security/
│   │   │   │   ├── config/
│   │   │   │   └── exception/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── assets/
│   └── package.json
├── docs/
│   ├── openapi.yaml
│   ├── schema.graphql
│   └── ADR.md
├── tests/
├── team.json
├── docker-compose.yml
├── Dockerfile
├── README.md
└── .gitignore
```
 
The repository may contain additional source, configuration, migration and test directories as implementation requires.
 
## Configuration and data
 
- Database credentials, JWT secrets, API keys and other sensitive values must be supplied through environment variables or an untracked local configuration file.
- Do not commit production secrets to GitHub.
- Provide an `.env.example` file containing variable names and safe sample values when environment variables are used.
- Demo or seeded restaurant records, menu prices, reviews and reservation slots must be clearly identified as sample data.
- Estimated costs and recommendation scores must be derived from the data available to the application.
- External restaurant-platform links must be configured and verified before they are displayed.
 
## Contribution workflow
 
All five team members contribute to the same GitHub repository using individual feature branches.
 
- Work on a feature branch instead of committing directly to `main`.
- Commit and push your own changes using your own GitHub account.
- Submit changes through pull requests.
- Review another team member's pull request before merging.
- Agree on API contracts, DTOs and database structures before parallel implementation.
- Update API documentation and tests when contracts change.
- Keep REST and GraphQL behavior consistent by using shared business services.
 
## Project documentation
 
- `README.md` — Project overview, team details and setup instructions.
- `team.json` — Team details and selected API approach.
- `docs/openapi.yaml` — REST API contract, endpoints and schemas.
- `docs/schema.graphql` — GraphQL queries, mutations, types and pagination.
- `docs/ADR.md` — Architecture Decision Record and design decisions.
 
## Important implementation notes
 
- Restaurant menus, prices, reviews and reservation slots are only as current as their configured data source.
- Sample data must be labelled as demonstration data.
- Do not claim that a restaurant booking or food order has been completed when the application only redirects to an external platform.
- Do not claim live availability unless a supported live integration provides it.
- REST and GraphQL APIs must share the same business logic.
- The final application must be tested from a clean clone of the repository before submission.
 
---
 
**Table4Us — Making group dining decisions easier!**
