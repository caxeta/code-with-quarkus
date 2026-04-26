package org.acme;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("admin")
public interface MyEntityResource extends PanacheEntityResource<MyEntity, Long> {
}
