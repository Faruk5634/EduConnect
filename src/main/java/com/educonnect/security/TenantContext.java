package com.educonnect.security;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> bypassTenant = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
        bypassTenant.remove();
    }

    public static void setBypassTenant(boolean bypass) {
        bypassTenant.set(bypass);
    }

    public static boolean isBypassTenant() {
        return Boolean.TRUE.equals(bypassTenant.get());
    }
}
