package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {

    // ⚡ Bolt: Cache the SecurityIdentity client proxy to eliminate expensive CDI context creation overhead on every transaction
    private volatile SecurityIdentity securityIdentityProxy;
    private volatile boolean proxyLookupAttempted = false;

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        try {
            if (securityIdentityProxy == null && !proxyLookupAttempted) {
                synchronized (this) {
                    if (securityIdentityProxy == null && !proxyLookupAttempted) {
                        BeanManager beanManager = CDI.current().getBeanManager();
                        Bean<?> securityIdentityBean = beanManager.resolve(beanManager.getBeans(SecurityIdentity.class));
                        if (securityIdentityBean != null) {
                            CreationalContext<?> creationalContext = beanManager.createCreationalContext(securityIdentityBean);
                            securityIdentityProxy = (SecurityIdentity) beanManager.getReference(securityIdentityBean, SecurityIdentity.class, creationalContext);
                        }
                        proxyLookupAttempted = true;
                    }
                }
            }

            if (securityIdentityProxy != null) {
                if (!securityIdentityProxy.isAnonymous() && securityIdentityProxy.getPrincipal() != null) {
                    customRevisionEntity.setUsername(securityIdentityProxy.getPrincipal().getName());
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
