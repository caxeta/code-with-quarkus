package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        try {
            SecurityIdentity securityIdentity = CDI.current().select(SecurityIdentity.class).get();

            if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
                customRevisionEntity.setUsername(securityIdentity.getPrincipal().getName());
            } else {
                customRevisionEntity.setUsername("anonymous");
            }
        } catch (Exception e) {
            // Fallback if CDI or SecurityIdentity is not available
            customRevisionEntity.setUsername("unknown");
        }
    }
}
