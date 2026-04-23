package org.acme;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.security.Authenticated;

@Authenticated
public interface MyEntityResource extends PanacheEntityResource<MyEntity, Long> {
}
