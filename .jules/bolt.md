## 2024-05-24 - Entity Caching in Quarkus
**Learning:** Hibernate ORM in Quarkus doesn't cache entities by default, leading to repeated database queries for the same entity data, which can become a bottleneck.
**Action:** Use `@Cacheable` from `jakarta.persistence` on Panache entities that are frequently read but rarely updated to utilize Hibernate's second-level cache.
