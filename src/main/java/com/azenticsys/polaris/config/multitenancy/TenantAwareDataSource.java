package com.azenticsys.polaris.config.multitenancy;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DataSource que intercepta cada getConnection() y aplica
 * SET search_path al schema del tenant activo en el thread.
 *
 * Flujo:
 *   - TenantContext tiene schema → search_path = {schema}, landlord, public
 *   - TenantContext null         → search_path = public, landlord  (modo landlord/startup)
 *
 * Las entidades landlord usan @Table(schema="landlord") → siempre acceden landlord.* sin importar search_path.
 * Las entidades tenant no tienen schema explícito → se resuelven vía search_path.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        applySearchPath(conn);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = super.getConnection(username, password);
        applySearchPath(conn);
        return conn;
    }

    private void applySearchPath(Connection conn) throws SQLException {
        String schema = TenantContext.get();
        String searchPath = (schema != null && !schema.isBlank())
                ? schema + ", landlord, public"
                : "public, landlord";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO " + searchPath);
        }
    }
}
