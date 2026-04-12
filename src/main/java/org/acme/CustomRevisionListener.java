package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        try {
            BeanManager beanManager = CDI.current().getBeanManager();
            Bean<?> bean = beanManager.resolve(beanManager.getBeans(SecurityIdentity.class));
            if (bean != null) {
                CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
                SecurityIdentity securityIdentity = (SecurityIdentity) beanManager.getReference(bean, SecurityIdentity.class, creationalContext);

                if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
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
