package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

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
@Table(indexes = {
    @Index(name = "idx_myentity_field", columnList = "field") // ⚡ Bolt: Add database index on frequently queried field to speed up search lookups
})
public class MyEntity extends PanacheEntity {
    // SECURITY: Limit input length to prevent DoS via excessively large payloads
    // SECURITY: Prevent Stored XSS and HTML Injection by restricting characters
    @Size(max = 255)
    // SECURITY: Block special characters to prevent XSS/injection payloads
    @Pattern(regexp = "^[A-Za-z0-9 _-]*$", message = "Input contains invalid characters")
    @Pattern(regexp = "^[^<>]*$", message = "Input must not contain HTML tags or script injection attempts")
    @Column(length = 255)
    public String field;
}
