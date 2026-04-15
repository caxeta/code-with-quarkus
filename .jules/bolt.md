## 2024-05-19 - Initial Analysis
**Learning:** Found a very simple Quarkus application. Need to identify a performance improvement without changing architecture.
**Action:** Let's look at `GreetingResource.java`. Adding `@ApplicationScoped` to JAX-RS resources in Quarkus prevents the framework from creating a new instance per request. Wait, `quarkus.resteasy.singleton-resources` defaults to `true` in Quarkus, meaning resources are singletons by default. So adding `@ApplicationScoped` might not do anything unless that's changed. But actually, `GreetingResource` is currently NOT annotated with any scope.

Wait, the prompt says:
> BOLT'S FAVORITE OPTIMIZATIONS:
> ...
> ⚡ Cache expensive API call results

Wait, looking at backend performance:
> - N+1 query problems in database calls
> - Missing database indexes on frequently queried fields
> - Expensive operations without caching
> - Synchronous operations that could be async

Let's look at `MyEntity.java`. It's a Panache entity.
```java
@Entity
public class MyEntity extends PanacheEntity {
    public String field;
}
```

Wait, what if we add `@Cacheable` to `MyEntity.java`?
"When an entity is annotated with `@Cacheable`, all its field values are cached except for collections and relations to other entities. This means the entity can be loaded without querying the database..."

Let's check if `quarkus-hibernate-orm-rest-data-panache` allows easy caching.
Wait, what if the optimization is to add `@Cacheable` to `MyEntity.java`?
If `MyEntity` is frequently accessed via REST, caching it makes it faster.
Another optimization is to make the RESTEasy resource `@ApplicationScoped` but wait, as I saw in the docs, `quarkus.resteasy.singleton-resources` is true by default. Is there another `@Cacheable`? Maybe in Panache?

Let's check `MyEntity.java`.
```java
@Entity
@Cacheable
public class MyEntity extends PanacheEntity {
    public String field;
}
```
Is `Cacheable` from `jakarta.persistence.Cacheable`?

Adding `@Cacheable` to `MyEntity` improves performance by allowing Hibernate to cache the entity in its second-level cache, avoiding repeated database round trips for frequently accessed entities.

This perfectly aligns with "Bolt's philosophy" of finding a simple (< 50 lines), low-risk, measurable performance improvement.

Let's quickly check the instructions. "Never add dependencies, modify build files (e.g., pom.xml), or make architectural changes without explicit instruction. Always add explanatory comments to optimizations."

I will add `@Cacheable` to `MyEntity.java`, with a comment explaining why it was added.
## 2024-05-24 - Entity Caching in Quarkus
**Learning:** Hibernate ORM in Quarkus doesn't cache entities by default, leading to repeated database queries for the same entity data, which can become a bottleneck.
**Action:** Use `@Cacheable` from `jakarta.persistence` on Panache entities that are frequently read but rarely updated to utilize Hibernate's second-level cache.

## 2024-05-25 - HTTP Compression
**Learning:** Quarkus does not enable HTTP response compression by default. Text-based payloads like JSON from REST endpoints can be compressed to save significant network bandwidth.
**Action:** Add `quarkus.http.enable-compression=true` to `application.properties` to easily compress large responses and improve overall network delivery time without architecture changes.

## 2024-05-26 - JDBC Batching
**Learning:** Hibernate ORM does not enable JDBC batching by default, which can lead to poor performance when inserting or updating multiple entities (like via Panache REST Data endpoints).
**Action:** Configure `quarkus.hibernate-orm.jdbc.statement-batch-size` to allow Hibernate to group multiple SQL statements into a single batch, reducing database roundtrips.

## 2026-04-03 - Hibernate Batch Fetching
**Learning:** By default, Hibernate ORM may suffer from the N+1 query problem when fetching collections or uninitialized associations, executing a separate query for each entity's relations.
**Action:** Configure `quarkus.hibernate-orm.fetch.batch-size` in `application.properties` to allow Hibernate to fetch associated entities in batches (e.g., 50 at a time), significantly reducing the number of database queries during list operations.
## 2026-04-13 - Caching CDI lookups in Envers RevisionListener
**Learning:** In Quarkus (and general CDI), performing programmatic CDI lookups (e.g. `CDI.current().getBeanManager().resolve(...)`) is an expensive operation. When implemented inside an Envers `RevisionListener` (which is invoked for *every* database revision), this causes massive performance degradation under load.
**Action:** Cache the `BeanManager` and `Bean` references in the `RevisionListener` instance to avoid the lookup penalty on every transaction, reducing revision overhead significantly.
## 2026-04-15 - CDI Lookup Overhead in Envers RevisionListener
**Learning:** Performing programmatic CDI lookups and context creation (`createCreationalContext()`, `getReference()`) inside an Envers `RevisionListener` is highly expensive and happens on every database transaction, causing significant performance degradation.
**Action:** Directly request and cache the CDI proxy (e.g., `CDI.current().select(SecurityIdentity.class).get()`) as a `volatile` field in the `RevisionListener`. Because the proxy dynamically delegates to the active context, this removes lookup overhead while remaining thread-safe.
