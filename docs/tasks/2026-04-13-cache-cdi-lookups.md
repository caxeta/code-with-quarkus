# 2026-04-13 Cache CDI Lookups

**Objective:**
Improve the database insertion and update performance by caching expensive programmatic CDI lookups within `CustomRevisionListener`.

**Changes Made:**
- Updated `src/main/java/org/acme/CustomRevisionListener.java` to cache `BeanManager` and `Bean` using `volatile` fields, performing lazy initialization to avoid invoking `CDI.current().getBeanManager().resolve()` on every revision.

**Tests:**
- Created and executed isolated benchmark test demonstrating significant performance improvement.
- Ran full `./mvnw clean test` test suite ensuring no regressions in validation, entity REST endpoints, or greeting endpoints.

**QA / Homologation Guide:**
- Perform load testing on database insertions to observe reduced overhead from Hibernate Envers `RevisionListener` executions.
