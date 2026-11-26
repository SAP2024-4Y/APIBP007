## 1. Business Problem & Consumers
DineIndia demonstrates how diners can discover restaurants in Indian cities and reserve a dining slot without contacting the restaurant by telephone. A customer needs to know the cuisine, approximate price and location before deciding where to eat. The primary consumer is a small browser application, while mobile applications and partner discovery services are potential consumers of the same interfaces. All seeded restaurants are fictional examples rather than verified commercial listings. The project intentionally excludes payments, menu ordering and delivery because they are separate business problems and would distract from the required API patterns.

## 2. Business Capabilities
The service enables restaurant discovery through filtering and sorting, geographical discovery through radius or bounding box searches, viewing scheduled dining availability, and authenticated reservation creation. A customer can move between pages without relying on offset pagination. The same booking rules apply to both interfaces, so switching from a REST client to a GraphQL client does not change the result. The interface displays prices in Indian rupees and converts UTC slot times into Indian Standard Time for diners.

## 3. Resource Model
A Restaurant has an identifier, name, city, category, status, indicative price and geographical location. Each restaurant has many Slots, and each slot can have at most one Booking. A slot represents one reservable table allocation for a fixed interval, not the capacity of an entire restaurant. A Booking identifies the slot and the authenticated customer. The GeoJSON Point object uses longitude before latitude. The GraphQL Restaurant type exposes its related slots, whereas REST exposes slots through the calendar collection. The SQLite database stores these resources and enforces uniqueness of the booking slot identifier.

## 4. Approach Decision
Approach A, REST-First + GraphQL Layer, was selected because the business operations are naturally resource oriented. FastAPI routes expose restaurant collections, calendar availability and booking creation. GraphQL resolvers call the same Python functions instead of making network calls to the REST endpoints. This avoids redundant serialization and keeps validation and authorization consistent. REST uses HTTP status codes to communicate outcomes; GraphQL uses structured extensions codes within errors. A GraphQL mutation can therefore communicate a booking conflict even when its HTTP response is successful. REST is versioned through its URI prefix. GraphQL evolves through additive fields and deprecation of the latitude field in favour of the location object.

## 5. Domain Pattern Mapping
List maps to restaurant discovery with status, category, minimum and maximum price filters. Sorting uses an allowlist of resource fields and a stable identifier as a tie breaker. The cursor encodes the last sort value and identifier rather than an offset. Map maps to restaurant locations and supports Haversine distance calculations in kilometres as well as bounding boxes. Calendar maps to scheduled dining slots, using half-open intervals and UTC timestamps. A slot overlaps a window when its start precedes the window end and its end follows the window start. A unique database constraint and immediate transaction prevent two customers from reserving the same slot simultaneously.

## 6. Composite Payload Comparison
The comparison uses GET /api/v1/restaurants?limit=1 followed by GET /api/v1/calendar?restaurant_id=1 versus POST /graphql requesting the first restaurant, its location and its slots. REST requires two round trips; GraphQL requires one. GraphQL can select only the name, identifier, coordinates and slot identifiers, while REST returns the full resource representation. The included measurement script reports actual response body bytes for each interface against the running service. Byte measurements must be recorded after running the service; no unverified measurements are claimed here. The comparison excludes transport headers and request body sizes and should be repeated with the same seeded database.

## 7. Error Catalogue
| Code / GraphQL extensions.code | REST status | Cause |
|---|---:|---|
| BAD_INPUT | 400 | Invalid request or sort |
| UNAUTHENTICATED | 401 | Missing or invalid credentials |
| FORBIDDEN | 403 | Customer attempts admin operation |
| NOT_FOUND | 404 | Unknown slot |
| CONFLICT | 409 | Slot already booked |
| INVALID_WINDOW | 422 | Invalid UTC interval |
| INVALID_CURSOR | 400 | Malformed or mismatched cursor |
| INVALID_LOCATION | 422 | Invalid coordinates or bounds |
| INVALID_PRICE | 422 | Negative or reversed price range |
| RATE_LIMITED | 429 | Request quota exceeded |
| INTERNAL_ERROR | 500 | Unexpected server failure |

## 8. Design Decisions
SQLite was selected to eliminate the need for a separate database container and simplify reproducible startup. A named Docker volume retains data between restarts, and idempotent inserts avoid duplicate seed rows. JWT bearer tokens distinguish customers from administrators. Demo credentials are deliberately documented for automated evaluation and should be disabled for a public deployment. The rate limiter uses a per-client fixed minute window and communicates the remaining quota in response headers. This is appropriate for a single service instance but a distributed deployment would require shared counters. GraphQL preloads slot rows once per request and groups them by restaurant identifier. This batching approach prevents the number of database queries from growing with the number of restaurants returned. It trades memory for predictable query count and is suitable for the small seeded dataset. The frontend uses ordinary HTML and JavaScript to avoid a separate build system and demonstrate direct API consumption.

## 9. Future Evolution
The first extension would support multiple tables and party sizes, with availability calculated for each table. A second extension would move spatial calculations and booking transactions to PostgreSQL with PostGIS for larger datasets. A third extension would replace the demo account with a proper identity provider and introduce distributed rate limiting. Existing GraphQL fields could remain available during these changes, while REST could introduce a new version for breaking changes.

## 10. Business Value
The service reduces the effort required to discover and reserve dining options. Shared business rules prevent inconsistent availability across consumer applications. Interactive documentation makes integration easier for developers, and the simple deployment allows the team to demonstrate the core business workflow without external infrastructure. The design is deliberately bounded to the academic use case rather than claiming readiness for a commercial restaurant network.
