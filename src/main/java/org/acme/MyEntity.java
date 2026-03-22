package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;

/**
 * Example JPA entity defined as a Panache Entity.
 * An ID field of Long type is provided, if you want to define your own ID field extends <code>PanacheEntityBase</code> instead.
 *
 * This uses the active record pattern, you can also use the repository pattern instead:
 * .
 *
 * Usage (more example on the documentation)
 *
 * {@code
 *     public void doSomething() {
 *         MyEntity entity1 = new MyEntity();
 *         entity1.field = "field-1";
 *         entity1.persist();
 *
 *         List<MyEntity> entities = MyEntity.listAll();
 *     }
 * }
 */
@Entity
@Cacheable // ⚡ Bolt: Enables Hibernate second-level cache to reduce database reads for frequent queries
public class MyEntity extends PanacheEntity {
    // SECURITY: Limit input length to prevent DoS via excessively large payloads
    @Size(max = 255)
    @Column(length = 255)
    public String field;
}
