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
