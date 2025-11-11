# SpaceX Dragon Rockets Repository

A simple Java 21 library (Maven) implementing an **in-memory repository** for SpaceX rockets and missions.  
The library follows **clean OO design**, **SOLID principles**, and **TDD** practices.

---

## Assumptions & Approach

- **In-memory storage**:
    - Thread-safe `ConcurrentHashMap` for repositories.
    - `Collections.synchronizedSet` for storing rocket IDs in missions.
- **Identifiers**:
    - `UUID` used for rockets and missions.
- **Entities**:
    - `Rocket` and `Mission` are created via static factory methods (`Rocket.create(name)` and `Mission.create(name)`).
    - Updates handled via repositories.
- **Facade**:
    - `SpaceXRepositoryFacade` provides all operations.
    - Repositories are abstracted via interfaces (`RocketRepository` and `MissionRepository`) for easy swapping (e.g., in-memory → database).

### Business Rules

- **Rockets**:
    - Initial status: `ON_GROUND`.
    - Can only be assigned to **one mission** at a time.
    - Statuses: `ON_GROUND`, `IN_SPACE`, `IN_REPAIR`.
- **Missions**:
    - Initial status: `SCHEDULED`.
    - Can have **multiple rockets**.
    - Statuses:
        - `SCHEDULED` — no rockets assigned.
        - `PENDING` — at least one rocket in `IN_REPAIR`.
        - `IN_PROGRESS` — at least one rocket assigned, none in repair.
        - `ENDED` — final stage, no more rocket assignments allowed.
- **Assignments**:
    - Rockets **cannot** be assigned to `ENDED` missions.
    - Mission status automatically recalculated after each rocket assignment or status change.
- **Summary**:
    - Missions sorted by:
        1. Number of rockets assigned (**descending**)
        2. Mission name (**descending alphabetical order**) if counts are equal.

---

## Build & Test

```bash
mvn clean install
mvn test
```

- Tests follow **TDD principles**.
- Each method has **positive, negative, and exceptional cases**.

---

## Notes & Design Choices

- **Clean OO Design**:
    - Facade orchestrates business logic.
    - Repositories abstract storage.
    - Entities encapsulate state.
- **Thread Safety**:
    - `ConcurrentHashMap` and `synchronizedSet` prevent concurrency issues.
- **Lombok**:
    - Used for builders and boilerplate reduction.
- **Extensibility**:
    - Repositories can be replaced with DB-backed implementations.
    - Facade remains unchanged.
- **Error Handling**:
    - Duplicate rockets/missions throw `IllegalStateException`.
    - Assignment to ended missions throws `IllegalStateException`.
    - Missing rockets/missions throw `IllegalArgumentException`.

---

## Example Usage

```java
RocketRepository rocketRepo = new InMemoryRocketRepository();
MissionRepository missionRepo = new InMemoryMissionRepository();
SpaceXRepositoryFacade facade = new SpaceXRepositoryFacade(rocketRepo, missionRepo);

// Add rockets and missions
Rocket dragon = facade.addRocket("Dragon 1");
Mission mars = facade.addMission("Mars Mission");

// Assign rocket to mission
facade.assignRocketToMission(dragon.getId(), mars.getId());

// Change rocket status
facade.changeRocketStatus(dragon.getId(), RocketStatus.IN_SPACE);

// Retrieve mission summary
List<Mission> summary = facade.summary();
summary.forEach(m -> System.out.println(m.getName() + " - " + m.getStatus()));
