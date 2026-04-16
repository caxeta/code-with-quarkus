package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {

    // ⚡ Bolt: Cache the CDI proxy directly to avoid expensive CDI lookups and context creation on every revision
    private volatile SecurityIdentity securityIdentity;

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        try {
            if (securityIdentity == null) {
                BeanManager beanManager = CDI.current().getBeanManager();
                Bean<?> securityIdentityBean = beanManager.resolve(beanManager.getBeans(SecurityIdentity.class));
                if (securityIdentityBean != null) {
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(securityIdentityBean);
                    securityIdentity = (SecurityIdentity) beanManager.getReference(securityIdentityBean, SecurityIdentity.class, creationalContext);
                }
            }

            if (securityIdentity != null) {
                if (!securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
                    customRevisionEntity.setUsername(securityIdentity.getPrincipal().getName());
                } else {
                    customRevisionEntity.setUsername("anonymous");
                }
            } else {
                customRevisionEntity.setUsername("system");
            }
        } catch (Exception e) {
            // Fallback if CDI or SecurityIdentity is not available
            customRevisionEntity.setUsername("unknown");
        }
    }
}
