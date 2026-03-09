package com.azenticsys.polaris.config.multitenancy;

/**
 * ThreadLocal que mantiene el nombre del schema de PostgreSQL activo para el request actual.
 * Valor típico: "t_torresytorres", "t_acme", etc.
 * Null = contexto landlord (schema: public + landlord).
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

    public static void set(String schemaName) {
        CURRENT_SCHEMA.set(schemaName);
    }

    public static String get() {
        return CURRENT_SCHEMA.get();
    }

    public static void clear() {
        CURRENT_SCHEMA.remove();
    }

    private TenantContext() {}
}
