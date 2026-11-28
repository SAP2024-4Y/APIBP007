# DineIndia demo walkthrough (Bruno, Swagger, GraphiQL)
Base URL: http://localhost:8080. Start with `docker compose up --build`. Swagger `/docs`; GraphiQL `/graphql`. All REST JSON bodies use Content-Type: application/json. In Bruno choose Auth > Bearer Token and paste only the token; in Swagger click Authorize and paste only the token. GraphiQL Headers: `{"Authorization":"Bearer YOUR_TOKEN"}`. Public reads need no token. Tokens expire after 3600 seconds. Demo credentials are local-only.

## Admin flow
1. POST `/api/v1/auth/token` body `{"username":"admin","password":"admin-password"}` → 200; copy token.
2. POST `/api/v1/restaurants` with admin token and body `{"name":"Demo Kitchen","city":"Chennai","category":"South Indian","status":"OPEN","price":500,"latitude":13.08,"longitude":80.27}` → 201; copy `created.id` (RID).
3. GET `/api/v1/restaurants/RID` → 200. GET `/api/v1/restaurants?limit=10&sort=id&order=desc` → new restaurant first. GET `/api/v1/restaurants?limit=10` then use response `after` as next request parameter; repeat until `has_more` false.
4. PATCH `/api/v1/restaurants/RID` admin body `{"price":600}` → 200.
5. POST `/api/v1/slots` admin body `{"restaurant_id":51,"starts_at":"2026-12-01T12:00:00Z","ends_at":"2026-12-01T13:00:00Z"}` → 201. Replace 51 with RID; choose future UTC date if necessary. Copy `created.id` (SID).
6. GET `/api/v1/calendar?restaurant_id=RID` → available true.

## Customer flow
1. POST `/api/v1/auth/token` body `{"username":"demo","password":"demo-password"}` → 200. Replace admin token with customer token.
2. GET `/api/v1/restaurants?city=Chennai&limit=10`; GET `/api/v1/restaurants/map?latitude=13.0827&longitude=80.2707&radius_km=10`; bounding box alternative `?min_lat=13&min_lon=80&max_lat=14&max_lon=81`.
3. POST `/api/v1/bookings` body `{"slot_id":1}` (replace 1 with SID) → 201; copy `created.id` (BID).
4. Repeat step 3 → 409 CONFLICT. GET calendar again → available false.
5. GET `/api/v1/bookings` customer token → own bookings.
6. DELETE `/api/v1/bookings/BID` customer token, no body → 200. Calendar now available true.
7. Switch back to admin token; DELETE `/api/v1/restaurants/RID` → 200. Do not delete seeded restaurants during demo.

## GraphQL equivalents
POST `/graphql` in Bruno (GraphQL body or JSON `{"query":"...","variables":{}}`). GraphiQL has preloaded named operations; choose one operation and provide variables in Variables panel. For login no header needed; copy token and set Headers for subsequent operations.
```graphql
mutation { login(username:"admin",password:"admin-password") { token token_type expires_in } }
mutation { login(username:"demo",password:"demo-password") { token } }
query { restaurants(first:10) { edges { cursor node { id name city category status price location { type coordinates } } } pageInfo { hasNextPage endCursor } } }
query { restaurants(first:10,after:"PASTE_CURSOR") { edges { node { id name } } pageInfo { hasNextPage endCursor } } }
query { restaurant(id:51) { id name price } }
query { map(latitude:13.0827,longitude:80.2707,radius_km:10) { id name location { coordinates } } }
mutation { addRestaurant(input:{name:"Graph Kitchen",city:"Chennai",category:"South Indian",price:500,latitude:13.08,longitude:80.27}) { created { id name } } }
mutation { updateRestaurant(id:51,input:{price:600}) { updated { id price } } }
mutation { addSlot(input:{restaurant_id:51,starts_at:"2026-12-01T12:00:00Z",ends_at:"2026-12-01T13:00:00Z"}) { created { id available } } }
query { calendar(restaurant_id:51) { id starts_at ends_at available } }
mutation { book(slot_id:"1") { created { id slot_id username } } }
query { bookings { id slot_id username } }
mutation { cancelBooking(id:1) { id } }
mutation { deleteRestaurant(id:51) { id } }
```
Run each operation separately; replace example IDs with actual IDs. For named AddSlot in sandbox variables example `{"id":51,"start":"2026-12-01T12:00:00Z","end":"2026-12-01T13:00:00Z"}`; Book variables `{"slot":"1"}`; UpdateRestaurant, Cancel and DeleteRestaurant variables `{"id":51}` (use appropriate resource ID).

## Error catalogue and demonstrations
REST error body contains code and usually message; GraphQL domain errors contain errors[0].extensions.code and http.status (GraphQL execution normally returns HTTP 200). Rate limit returns HTTP 429. Use valid token where required.

| Code | HTTP | REST request | JSON body / notes | GraphQL equivalent (operation) |
|---|---|---|---|---|
| BAD_INPUT | 400 | GET /api/v1/restaurants?limit=0 | none | `{ restaurants(first:0){edges{cursor}} }` |
| UNAUTHENTICATED | 401 | POST /api/v1/auth/token | `{"username":"demo","password":"wrong"}` | `mutation {login(username:"demo",password:"wrong"){token}}` |
| FORBIDDEN | 403 | POST /api/v1/restaurants | valid admin creation body above, but customer token | addRestaurant with customer token |
| NOT_FOUND | 404 | GET /api/v1/restaurants/999999 | none | `{restaurant(id:999999){id}}` |
| CONFLICT | 409 | POST /api/v1/bookings | `{"slot_id":1}` use already booked SID | `mutation {book(slot_id:"1"){created{id}}}` use booked SID |
| INVALID_WINDOW | 422 | GET /api/v1/calendar?starts_at=2026-12-02T00:00:00Z&ends_at=2026-12-01T00:00:00Z | none | `{calendar(starts_at:"2026-12-02T00:00:00Z",ends_at:"2026-12-01T00:00:00Z"){id}}` |
| INVALID_CURSOR | 400 | GET /api/v1/restaurants?after=invalid | none | `{restaurants(after:"invalid"){edges{cursor}}}` |
| INVALID_LOCATION | 422 | GET /api/v1/restaurants/map?latitude=100&longitude=80&radius_km=10 | none | `{map(latitude:100,longitude:80,radius_km:10){id}}` |
| INVALID_PRICE | 422 | GET /api/v1/restaurants?min_price=-1 | none | `{restaurants(min_price:-1){edges{cursor}}}` |
| RATE_LIMITED | 429 | GET /health repeatedly | none; exceed 120 requests in same fixed minute locally; check Retry-After | repeat POST /graphql with `{restaurants(first:1){edges{cursor}}}` |
| INTERNAL_ERROR | 500 | unexpected server failure | no intentional public trigger; automated fault injection only | automated fault injection only |

Also demonstrate missing token and invalid token by sending POST bookings with valid SID and no Authorization or `Bearer invalid`. Both return 401. GET requests do not require JSON bodies. Never deliberately break the live database to demonstrate 500.
