package com.educonnect.aspect;

import com.educonnect.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class    HibernateFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.educonnect.repository.*.*(..))")
    public void enableTenantFilter() {
        if (!TenantContext.isBypassTenant()) {
            Long tenantId = TenantContext.getCurrentTenant();
            if (tenantId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("schoolId", tenantId);
            }
        }
    }
}
