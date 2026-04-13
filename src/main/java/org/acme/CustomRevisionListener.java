package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {

    // ⚡ Bolt: Cache BeanManager and Bean to avoid expensive CDI lookups on every revision creation
    private volatile BeanManager beanManager;
    private volatile Bean<?> securityIdentityBean;

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        try {
            if (beanManager == null) {
                beanManager = CDI.current().getBeanManager();
            }
            if (securityIdentityBean == null) {
                securityIdentityBean = beanManager.resolve(beanManager.getBeans(SecurityIdentity.class));
            }

            if (securityIdentityBean != null) {
                CreationalContext<?> creationalContext = beanManager.createCreationalContext(securityIdentityBean);
                SecurityIdentity securityIdentity = (SecurityIdentity) beanManager.getReference(securityIdentityBean, SecurityIdentity.class, creationalContext);

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
