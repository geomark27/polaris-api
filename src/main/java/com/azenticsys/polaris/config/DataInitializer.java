package com.azenticsys.polaris.config;

import com.azenticsys.polaris.systemvalue.entity.SystemValue;
import com.azenticsys.polaris.systemvalue.repository.SystemValueRepository;
import com.azenticsys.polaris.user.entity.User;
import com.azenticsys.polaris.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemValueRepository systemValueRepository;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@polaris.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin123.}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSystemValues();
    }

    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created — username: '{}', email: '{}'", adminUsername, adminEmail);
        } else {
            log.debug("DataInitializer: users already exist, skipping user seed.");
        }
    }

    private void seedSystemValues() {
        if (systemValueRepository.count() > 0) {
            log.debug("DataInitializer: system values already exist, skipping seed.");
            return;
        }

        List<SystemValue> values = List.of(

                // ── PRODUCT_TYPE ──────────────────────────────────────────
                sv("PRODUCT_TYPE", "STANDARD",     "Producto Estándar",      "Producto físico que se almacena y vende",         0),
                sv("PRODUCT_TYPE", "SERVICE",      "Servicio",               "Producto intangible, no genera stock",            1),
                sv("PRODUCT_TYPE", "DIGITAL",      "Producto Digital",       "Descarga o licencia digital",                    2),
                sv("PRODUCT_TYPE", "RAW_MATERIAL", "Materia Prima",          "Insumo usado en manufactura",                    3),

                // ── PRODUCT_TRACKING ──────────────────────────────────────
                sv("PRODUCT_TRACKING", "NONE",   "Sin Trazabilidad",  "No se rastrea por lote ni serie",                  0),
                sv("PRODUCT_TRACKING", "LOT",    "Por Lote",          "Se rastrea por número de lote",                    1),
                sv("PRODUCT_TRACKING", "SERIAL", "Por Número de Serie","Se rastrea unidad a unidad por número de serie",  2),

                // ── UOM (Unidad de Medida) ────────────────────────────────
                sv("UOM", "PCS",  "Piezas",      "Unidad discreta genérica",   0),
                sv("UOM", "KG",   "Kilogramos",  "Peso en kilogramos",         1),
                sv("UOM", "LT",   "Litros",      "Volumen en litros",          2),
                sv("UOM", "MT",   "Metros",      "Longitud en metros",         3),
                sv("UOM", "BOX",  "Caja",        "Unidad de embalaje",         4),
                sv("UOM", "HOUR", "Hora",        "Unidad de tiempo (servicios)", 5),

                // ── CURRENCY ──────────────────────────────────────────────
                sv("CURRENCY", "USD", "Dólar Estadounidense", "Moneda base del sistema", 0),
                sv("CURRENCY", "PEN", "Sol Peruano",          "Moneda local Perú",       1),
                sv("CURRENCY", "EUR", "Euro",                 "Moneda de la Unión Europea", 2),

                // ── DOCUMENT_STATUS ───────────────────────────────────────
                sv("DOCUMENT_STATUS", "DRAFT",     "Borrador",    "Documento en edición, no confirmado", 0),
                sv("DOCUMENT_STATUS", "CONFIRMED", "Confirmado",  "Documento aprobado y en proceso",     1),
                sv("DOCUMENT_STATUS", "DONE",      "Completado",  "Documento finalizado correctamente",  2),
                sv("DOCUMENT_STATUS", "CANCELLED", "Cancelado",   "Documento anulado",                   3)
        );

        systemValueRepository.saveAll(values);
        log.info("DataInitializer: {} system values seeded across 5 catalogs.", values.size());
    }

    private SystemValue sv(String catalogType, String value, String label, String description, int order) {
        return SystemValue.builder()
                .catalogType(catalogType)
                .value(value)
                .label(label)
                .description(description)
                .displayOrder(order)
                .build();
    }
}
