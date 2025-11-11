# SpaceX Dragon Rockets Repository


Simple Java 21 library (Maven) implementing an in-memory repository for rockets and missions.

---

## Assumptions & Approach


- In-memory store using thread-safe `ConcurrentHashMap` and `Collections.synchronizedSet` for simplicity.
- `UUID` used as identifiers for rockets and missions.
- Library exposes a single facade: `SpaceXRepositoryFacade` implementing required operations.
- Business rules implemented according to specification:
- Adding a rocket starts with status `ON_GROUND`.
- Adding a mission starts with status `SCHEDULED`.
- Rocket can be assigned to at most one mission.
- Mission can have multiple rockets.
- If any rocket in a mission changes status to `IN_REPAIR`, the mission becomes `PENDING`.
- If a mission has at least one rocket and none are `IN_REPAIR`, mission becomes `IN_PROGRESS`.
- If mission is `ENDED`, assignment attempts fail.
- Summary sorted by number of rockets descending; when equal, mission names in descending alphabetical order (Z..A).

---

## Build & Test


```bash
mvn clean test
```

---

## Notes

Uses Lombok to reduce boilerplate.

Clean OO design: small classes, single responsibility, and a facade to orchestrate operations.

Tests included demonstrate positive, negative, and exceptional scenarios.