package com.azenticsys.polaris.config.multitenancy;

import com.azenticsys.polaris.tenant.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de prioridad alta que resuelve el tenant desde el header X-Tenant-ID.
 * Corre antes del filtro de seguridad Spring.
 *
 * Flujo:
 *  1. Lee X-Tenant-ID (slug del tenant, ej: "torresytorres")
 *  2. Busca el tenant en landlord.tenants (funciona sin TenantContext porque la tabla tiene schema explícito)
 *  3. Si existe, setea TenantContext con el schemaName del tenant
 *  4. Si no existe o no hay header, TenantContext queda null (modo landlord)
 *  5. Al final del request, limpia el TenantContext del thread
 *
 * NOTA: JwtAuthenticationFilter puede sobreescribir el TenantContext con el valor del JWT,
 * que es más seguro para requests autenticados.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String slug = request.getHeader(TENANT_HEADER);

            if (slug != null && !slug.isBlank()) {
                tenantRepository.findBySlugAndIsActiveTrue(slug)
                        .ifPresent(tenant -> TenantContext.set(tenant.getSchemaName()));
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
