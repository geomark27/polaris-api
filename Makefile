.PHONY: help setup build test run clean \
        docker-up docker-down docker-clean docker-logs \
        db-connect db-reset \
        compile package install install-with-tests \
        test-unit test-integration \
        start quick-start dev ci \
        clean-all fresh-install \
        status dependencies update restart \
        push pull git-status sync \
        module

# ============================================================
# Variables
# ============================================================
COMPOSE    = docker compose --env-file .env
DB_NAME    = $(shell grep ^DB_NAME .env 2>/dev/null | cut -d= -f2 | tr -d ' ' || echo polaris_db)
DB_USER    = $(shell grep ^DB_USER .env 2>/dev/null | cut -d= -f2 | tr -d ' ' || echo postgres)
JAR        = target/polaris-api-0.0.1-SNAPSHOT.jar
BRANCH     = $(shell git branch --show-current 2>/dev/null || echo main)
EXPORT_ENV = export $$(grep -v '^\#' .env | grep -v '^\s*$$' | xargs) &&

# ============================================================
# Colores
# ============================================================
BLUE   = \033[0;34m
GREEN  = \033[0;32m
YELLOW = \033[1;33m
RED    = \033[0;31m
NC     = \033[0m

# ============================================================
# AYUDA
# ============================================================

## help: Muestra esta ayuda
help:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)          POLARIS ERP - Comandos Make                  $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(GREEN)PRIMERA VEZ (Setup Inicial):$(NC)"
	@echo "  $(YELLOW)make setup$(NC)              - Setup completo del proyecto"
	@echo "  $(YELLOW)make docker-up$(NC)          - Levantar PostgreSQL"
	@echo "  $(YELLOW)make run$(NC)                - Ejecutar aplicacion"
	@echo ""
	@echo "$(GREEN)RAPIDO (Una sola linea):$(NC)"
	@echo "  $(YELLOW)make start$(NC)              - Docker up + Compilar + Ejecutar"
	@echo ""
	@echo "$(GREEN)DESARROLLO DIARIO:$(NC)"
	@echo "  $(YELLOW)make dev$(NC)                - Compilar + Docker up + Ejecutar"
	@echo "  $(YELLOW)make compile$(NC)            - Solo compilar cambios"
	@echo "  $(YELLOW)make test$(NC)               - Ejecutar todos los tests"
	@echo "  $(YELLOW)make test-unit$(NC)          - Solo tests unitarios"
	@echo "  $(YELLOW)make test-integration$(NC)   - Solo tests de integracion"
	@echo ""
	@echo "$(GREEN)BASE DE DATOS:$(NC)"
	@echo "  $(YELLOW)make db-up$(NC)              - Levantar PostgreSQL"
	@echo "  $(YELLOW)make db-down$(NC)            - Detener servicios"
	@echo "  $(YELLOW)make db-clean$(NC)           - Eliminar contenedores y volumenes"
	@echo "  $(YELLOW)make db-logs$(NC)            - Ver logs en tiempo real"
	@echo "  $(YELLOW)make restart$(NC)            - Reiniciar servicios"
	@echo "  $(YELLOW)make db-connect$(NC)         - Conectar a PostgreSQL (psql)"
	@echo "  $(YELLOW)make db-reset$(NC)           - Resetear base de datos"
	@echo ""
	@echo "$(GREEN)BUILD:$(NC)"
	@echo "  $(YELLOW)make compile$(NC)            - Compilar sin tests"
	@echo "  $(YELLOW)make package$(NC)            - Generar JAR"
	@echo "  $(YELLOW)make install$(NC)            - Instalar en repo Maven local"
	@echo "  $(YELLOW)make run-jar$(NC)            - Ejecutar JAR generado"
	@echo "  $(YELLOW)make run-prod$(NC)           - Ejecutar con perfil prod"
	@echo ""
	@echo "$(GREEN)LIMPIEZA:$(NC)"
	@echo "  $(YELLOW)make clean$(NC)              - Limpiar archivos Maven"
	@echo "  $(YELLOW)make clean-all$(NC)          - Limpieza profunda"
	@echo "  $(YELLOW)make fresh-install$(NC)      - Reinstalar desde cero"
	@echo ""
	@echo "$(GREEN)UTILIDADES:$(NC)"
	@echo "  $(YELLOW)make status$(NC)             - Estado de servicios Docker"
	@echo "  $(YELLOW)make dependencies$(NC)       - Arbol de dependencias"
	@echo "  $(YELLOW)make update$(NC)             - Ver actualizaciones disponibles"
	@echo "  $(YELLOW)make ci$(NC)                 - Flujo CI (compile + test + package)"
	@echo ""
	@echo "$(GREEN)GIT:$(NC)"
	@echo "  $(YELLOW)make push m='mensaje'$(NC)   - Add + Commit + Push"
	@echo "  $(YELLOW)make pull$(NC)               - Pull desde origin"
	@echo "  $(YELLOW)make git-status$(NC)         - Ver estado del repositorio"
	@echo "  $(YELLOW)make sync m='mensaje'$(NC)   - Pull + Commit + Push"
	@echo ""
	@echo "$(GREEN)GENERADOR DE MODULOS:$(NC)"
	@echo "  $(YELLOW)make module name=product$(NC)   - Genera estructura base de un modulo"
	@echo "  $(RED)IMPORTANTE: usa el nombre en singular (product, not products)$(NC)"
	@echo ""
	@echo "$(BLUE)Usa 'make <comando>' para ejecutar$(NC)"
	@echo ""

# ============================================================
# SETUP INICIAL
# ============================================================

## setup: Configuracion inicial del proyecto (primera vez)
setup:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)          SETUP INICIAL - POLARIS ERP                  $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 1/4:$(NC) Verificando archivo .env..."
	@test -f .env || ( \
		echo "$(RED)ERROR: .env no encontrado$(NC)" && \
		echo "$(YELLOW)Copia .env.example a .env y configuralo$(NC)" && \
		exit 1 \
	)
	@echo "$(GREEN)  OK .env existe$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 2/4:$(NC) Descargando dependencias de Maven..."
	@./mvnw dependency:resolve dependency:resolve-plugins -q
	@echo "$(GREEN)  OK Dependencias descargadas$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 3/4:$(NC) Compilando proyecto..."
	@./mvnw clean compile -q
	@echo "$(GREEN)  OK Proyecto compilado$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 4/4:$(NC) Instalando en repositorio Maven local..."
	@./mvnw install -DskipTests -q
	@echo "$(GREEN)  OK Proyecto instalado$(NC)"
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)        SETUP COMPLETADO EXITOSAMENTE                  $(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(BLUE)Proximos pasos:$(NC)"
	@echo "  $(YELLOW)make docker-up$(NC)   -> Levantar PostgreSQL"
	@echo "  $(YELLOW)make run$(NC)         -> Ejecutar aplicacion"
	@echo "  $(YELLOW)make help$(NC)        -> Ver todos los comandos"
	@echo ""

# ============================================================
# BASE DE DATOS (DOCKER)
# ============================================================

## db-up: Levanta PostgreSQL
db-up:
	@echo "$(GREEN)Levantando PostgreSQL con Docker Compose...$(NC)"
	@$(COMPOSE) up -d postgres
	@echo "$(GREEN)OK Servicios iniciados$(NC)"
	@echo "$(BLUE)   PostgreSQL: localhost:5433 (DB: $(DB_NAME))$(NC)"

## db-down: Detiene todos los servicios
db-down:
	@echo "$(YELLOW)Deteniendo servicios...$(NC)"
	@$(COMPOSE) down
	@echo "$(GREEN)OK Servicios detenidos$(NC)"

## db-clean: Elimina contenedores y volumenes (borra datos)
db-clean:
	@echo "$(RED)Eliminando contenedores y volumenes...$(NC)"
	@$(COMPOSE) down -v
	@echo "$(GREEN)OK Limpieza completada$(NC)"

## db-logs: Muestra logs de todos los servicios en tiempo real
db-logs:
	@echo "$(BLUE)Logs en tiempo real (Ctrl+C para salir):$(NC)"
	@$(COMPOSE) logs -f

## restart: Reinicia los servicios Docker
restart:
	@echo "$(YELLOW)Reiniciando servicios...$(NC)"
	@$(COMPOSE) restart
	@echo "$(GREEN)OK Servicios reiniciados$(NC)"

## db-connect: Conecta a PostgreSQL via psql
db-connect:
	@echo "$(BLUE)Conectando a PostgreSQL...$(NC)"
	@$(COMPOSE) exec postgres psql -U $(DB_USER) -d $(DB_NAME)

## db-reset: Resetea la base de datos (elimina todos los datos)
db-reset:
	@echo "$(RED)Reseteando base de datos...$(NC)"
	@$(COMPOSE) down -v
	@$(COMPOSE) up -d postgres
	@echo "$(YELLOW)Esperando que PostgreSQL este listo...$(NC)"
	@sleep 4
	@echo "$(GREEN)OK Base de datos reseteada$(NC)"

# ============================================================
# BUILD Y MAVEN
# ============================================================

## compile: Compila el proyecto sin ejecutar tests
compile:
	@echo "$(GREEN)Compilando proyecto...$(NC)"
	@./mvnw clean compile
	@echo "$(GREEN)OK Compilacion exitosa$(NC)"

## package: Empaqueta el proyecto en JAR (sin tests)
package:
	@echo "$(GREEN)Empaquetando proyecto...$(NC)"
	@./mvnw clean package -DskipTests
	@echo "$(GREEN)OK JAR generado en target/$(NC)"

## install: Instala en el repositorio local de Maven (sin tests)
install:
	@echo "$(GREEN)Instalando proyecto...$(NC)"
	@./mvnw clean install -DskipTests
	@echo "$(GREEN)OK Proyecto instalado$(NC)"

## install-with-tests: Instala ejecutando todos los tests
install-with-tests:
	@echo "$(GREEN)Instalando proyecto con tests...$(NC)"
	@./mvnw clean install
	@echo "$(GREEN)OK Proyecto instalado con tests$(NC)"

# ============================================================
# TESTS
# ============================================================

## test: Ejecuta todos los tests
test:
	@echo "$(GREEN)Ejecutando tests...$(NC)"
	@./mvnw test

## test-unit: Ejecuta solo tests unitarios
test-unit:
	@echo "$(GREEN)Ejecutando tests unitarios...$(NC)"
	@./mvnw test -Dtest="**/*Test"

## test-integration: Ejecuta solo tests de integracion
test-integration:
	@echo "$(GREEN)Ejecutando tests de integracion...$(NC)"
	@./mvnw test -Dtest="**/*IT"

# ============================================================
# EJECUCION
# ============================================================

## run: Ejecuta la aplicacion en modo desarrollo
run:
	@echo "$(GREEN)Iniciando aplicacion...$(NC)"
	@$(EXPORT_ENV) ./mvnw spring-boot:run

## run-prod: Ejecuta con perfil de produccion
run-prod:
	@echo "$(GREEN)Iniciando aplicacion (produccion)...$(NC)"
	@$(EXPORT_ENV) ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

## run-jar: Empaqueta y ejecuta el JAR directamente
run-jar: package
	@echo "$(GREEN)Ejecutando JAR...$(NC)"
	@$(EXPORT_ENV) java -jar $(JAR)

## start: Comando completo - Docker up + Compilar + Ejecutar
start: docker-up compile run

## dev: Inicio rapido para desarrollo diario
dev:
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)            INICIO RAPIDO - POLARIS ERP                $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 1/3:$(NC) Levantando infraestructura..."
	@$(MAKE) docker-up
	@echo ""
	@echo "$(YELLOW)Paso 2/3:$(NC) Compilando cambios..."
	@./mvnw compile -q
	@echo "$(GREEN)  OK Compilacion exitosa$(NC)"
	@echo ""
	@echo "$(YELLOW)Paso 3/3:$(NC) Iniciando aplicacion..."
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@$(EXPORT_ENV) ./mvnw spring-boot:run

# ============================================================
# LIMPIEZA
# ============================================================

## clean: Limpia archivos generados por Maven
clean:
	@echo "$(YELLOW)Limpiando archivos generados...$(NC)"
	@./mvnw clean
	@echo "$(GREEN)OK Limpieza completada$(NC)"

## clean-all: Limpieza profunda (Maven + cache local del proyecto)
clean-all:
	@echo "$(RED)Limpieza profunda del proyecto...$(NC)"
	@echo "$(YELLOW)  -> Limpiando Maven...$(NC)"
	@./mvnw clean
	@echo "$(YELLOW)  -> Eliminando artefactos locales del proyecto...$(NC)"
	@rm -rf ~/.m2/repository/com/azenticsys/polaris-api
	@echo "$(YELLOW)  -> Limpiando cache de IDE...$(NC)"
	@rm -rf target/
	@echo "$(GREEN)OK Limpieza profunda completada$(NC)"

## fresh-install: Reinstala TODO desde cero
fresh-install: clean-all
	@echo "$(GREEN)Instalacion desde cero...$(NC)"
	@echo "$(YELLOW)  -> Descargando dependencias...$(NC)"
	@./mvnw dependency:resolve dependency:resolve-plugins
	@echo "$(YELLOW)  -> Compilando...$(NC)"
	@./mvnw clean compile
	@echo "$(YELLOW)  -> Instalando en repositorio local...$(NC)"
	@./mvnw install -DskipTests
	@echo "$(GREEN)OK Instalacion fresca completada$(NC)"

# ============================================================
# UTILIDADES
# ============================================================

## status: Muestra el estado de los contenedores Docker
status:
	@echo "$(BLUE)Estado de servicios:$(NC)"
	@echo ""
	@$(COMPOSE) ps

## dependencies: Muestra el arbol de dependencias Maven
dependencies:
	@echo "$(BLUE)Arbol de dependencias:$(NC)"
	@./mvnw dependency:tree

## update: Muestra actualizaciones disponibles para dependencias
update:
	@echo "$(GREEN)Actualizaciones disponibles:$(NC)"
	@./mvnw versions:display-dependency-updates

## ci: Flujo completo de CI (compile + test + package)
ci: compile test package
	@echo "$(GREEN)OK Build de CI exitoso$(NC)"

# ============================================================
# GIT
# ============================================================

## push: Push rapido con mensaje - Uso: make push m="tu mensaje"
push:
	@if [ -z "$(m)" ]; then \
		echo "$(RED)Error: Debes proporcionar un mensaje$(NC)"; \
		echo "$(YELLOW)   Uso: make push m='tu mensaje de commit'$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Agregando archivos...$(NC)"
	@git add .
	@echo "$(GREEN)Commiteando: $(m)$(NC)"
	@git commit -m "$(m)"
	@echo "$(GREEN)Pusheando a origin/$(BRANCH)...$(NC)"
	@git push origin $(BRANCH)
	@echo "$(GREEN)OK Push completado!$(NC)"

## pull: Pull desde origin
pull:
	@echo "$(GREEN)Pulling desde origin/$(BRANCH)...$(NC)"
	@git pull origin $(BRANCH)
	@echo "$(GREEN)OK Pull completado!$(NC)"

## git-status: Ver estado del repositorio
git-status:
	@echo "$(BLUE)Estado de Git (rama: $(BRANCH)):$(NC)"
	@echo ""
	@git status

## sync: Sincronizar (pull + commit + push) - Uso: make sync m="tu mensaje"
sync:
	@if [ -z "$(m)" ]; then \
		echo "$(RED)Error: Debes proporcionar un mensaje$(NC)"; \
		echo "$(YELLOW)   Uso: make sync m='tu mensaje de commit'$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Pulling cambios...$(NC)"
	@git pull origin $(BRANCH)
	@echo "$(GREEN)Agregando archivos...$(NC)"
	@git add .
	@echo "$(GREEN)Commiteando: $(m)$(NC)"
	@git commit -m "$(m)"
	@echo "$(GREEN)Pusheando a origin/$(BRANCH)...$(NC)"
	@git push origin $(BRANCH)
	@echo "$(GREEN)OK Sincronizacion completada!$(NC)"

# ============================================================
# GENERADOR DE MODULOS
# ============================================================

# Variables internas del generador
BASE_PKG_PATH = src/main/java/com/azenticsys/polaris
BASE_PKG      = com.azenticsys.polaris

## module: Genera la estructura base de un modulo - Uso: make module name=product (singular)
module:
	@if [ -z "$(name)" ]; then \
		echo "$(RED)Error: Debes proporcionar un nombre en singular$(NC)"; \
		echo "$(YELLOW)   Uso: make module name=product$(NC)"; \
		exit 1; \
	fi
	$(eval NAME_LOWER  := $(shell echo "$(name)" | tr '[:upper:]' '[:lower:]'))
	$(eval NAME_UPPER  := $(shell echo "$(name)" | sed 's/\b./\u&/g'))
	$(eval MODULE_PATH := $(BASE_PKG_PATH)/$(NAME_LOWER))
	@if [ -d "$(MODULE_PATH)" ]; then \
		echo "$(RED)Error: El modulo '$(NAME_LOWER)' ya existe en $(MODULE_PATH)$(NC)"; \
		exit 1; \
	fi
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)     Generando modulo: $(NAME_UPPER)                  $(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)Creando estructura de carpetas...$(NC)"
	@mkdir -p $(MODULE_PATH)/entity
	@mkdir -p $(MODULE_PATH)/repository
	@mkdir -p $(MODULE_PATH)/service
	@mkdir -p $(MODULE_PATH)/controller
	@mkdir -p $(MODULE_PATH)/dto
	@echo "$(GREEN)  OK Carpetas creadas$(NC)"
	@echo ""
	@echo "$(YELLOW)Generando Entity...$(NC)"
	@printf 'package $(BASE_PKG).$(NAME_LOWER).entity;\n\nimport jakarta.persistence.*;\nimport lombok.*;\nimport org.hibernate.annotations.UuidGenerator;\n\nimport java.time.LocalDateTime;\nimport java.util.UUID;\n\n@Entity\n@Table(name = "$(NAME_LOWER)s")\n@Getter\n@Setter\n@NoArgsConstructor\n@AllArgsConstructor\n@Builder\npublic class $(NAME_UPPER) {\n\n    @Id\n    @GeneratedValue\n    @UuidGenerator\n    @Column(name = "id", updatable = false, nullable = false)\n    private UUID id;\n\n    // TODO: agregar campos del dominio\n\n    @Column(name = "is_active", nullable = false)\n    @Builder.Default\n    private boolean isActive = true;\n\n    @Column(name = "created_at", nullable = false, updatable = false)\n    private LocalDateTime createdAt;\n\n    @Column(name = "updated_at", nullable = false)\n    private LocalDateTime updatedAt;\n\n    @Column(name = "deleted_at")\n    private LocalDateTime deletedAt;\n\n    @PrePersist\n    protected void onCreate() {\n        createdAt = LocalDateTime.now();\n        updatedAt = LocalDateTime.now();\n    }\n\n    @PreUpdate\n    protected void onUpdate() {\n        updatedAt = LocalDateTime.now();\n    }\n}\n' > $(MODULE_PATH)/entity/$(NAME_UPPER).java
	@echo "$(GREEN)  OK $(NAME_UPPER).java$(NC)"
	@echo "$(YELLOW)Generando Repository...$(NC)"
	@printf 'package $(BASE_PKG).$(NAME_LOWER).repository;\n\nimport $(BASE_PKG).$(NAME_LOWER).entity.$(NAME_UPPER);\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\n\nimport java.util.UUID;\n\n@Repository\npublic interface $(NAME_UPPER)Repository extends JpaRepository<$(NAME_UPPER), UUID> {\n\n    // TODO: agregar queries personalizados\n}\n' > $(MODULE_PATH)/repository/$(NAME_UPPER)Repository.java
	@echo "$(GREEN)  OK $(NAME_UPPER)Repository.java$(NC)"
	@echo "$(YELLOW)Generando DTOs...$(NC)"
	@printf 'package $(BASE_PKG).$(NAME_LOWER).dto;\n\nimport jakarta.validation.constraints.NotBlank;\n\npublic record Create$(NAME_UPPER)Request(\n\n        // TODO: agregar campos con sus validaciones\n        @NotBlank(message = "Field is required")\n        String name\n) {}\n' > $(MODULE_PATH)/dto/Create$(NAME_UPPER)Request.java
	@printf 'package $(BASE_PKG).$(NAME_LOWER).dto;\n\npublic record Update$(NAME_UPPER)Request(\n\n        // TODO: agregar campos actualizables\n        String name\n) {}\n' > $(MODULE_PATH)/dto/Update$(NAME_UPPER)Request.java
	@printf 'package $(BASE_PKG).$(NAME_LOWER).dto;\n\nimport $(BASE_PKG).$(NAME_LOWER).entity.$(NAME_UPPER);\n\nimport java.time.LocalDateTime;\nimport java.util.UUID;\n\npublic record $(NAME_UPPER)Response(\n        UUID id,\n        // TODO: agregar campos de la respuesta\n        boolean isActive,\n        LocalDateTime createdAt,\n        LocalDateTime updatedAt\n) {\n    public static $(NAME_UPPER)Response from($(NAME_UPPER) entity) {\n        return new $(NAME_UPPER)Response(\n                entity.getId(),\n                // TODO: mapear campos\n                entity.isActive(),\n                entity.getCreatedAt(),\n                entity.getUpdatedAt()\n        );\n    }\n}\n' > $(MODULE_PATH)/dto/$(NAME_UPPER)Response.java
	@echo "$(GREEN)  OK DTOs generados$(NC)"
	@echo "$(YELLOW)Generando Service...$(NC)"
	@printf 'package $(BASE_PKG).$(NAME_LOWER).service;\n\nimport $(BASE_PKG).$(NAME_LOWER).dto.Create$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.Update$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.$(NAME_UPPER)Response;\n\nimport java.util.List;\nimport java.util.UUID;\n\npublic interface $(NAME_UPPER)Service {\n\n    $(NAME_UPPER)Response create(Create$(NAME_UPPER)Request request);\n\n    $(NAME_UPPER)Response findById(UUID id);\n\n    List<$(NAME_UPPER)Response> findAll();\n\n    $(NAME_UPPER)Response update(UUID id, Update$(NAME_UPPER)Request request);\n\n    void softDelete(UUID id);\n}\n' > $(MODULE_PATH)/service/$(NAME_UPPER)Service.java
	@printf 'package $(BASE_PKG).$(NAME_LOWER).service;\n\nimport $(BASE_PKG).$(NAME_LOWER).dto.Create$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.Update$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.$(NAME_UPPER)Response;\nimport $(BASE_PKG).$(NAME_LOWER).entity.$(NAME_UPPER);\nimport $(BASE_PKG).$(NAME_LOWER).repository.$(NAME_UPPER)Repository;\nimport lombok.RequiredArgsConstructor;\nimport org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Transactional;\n\nimport java.time.LocalDateTime;\nimport java.util.List;\nimport java.util.UUID;\n\n@Service\n@RequiredArgsConstructor\npublic class $(NAME_UPPER)ServiceImpl implements $(NAME_UPPER)Service {\n\n    private final $(NAME_UPPER)Repository $(NAME_LOWER)Repository;\n\n    @Override\n    @Transactional\n    public $(NAME_UPPER)Response create(Create$(NAME_UPPER)Request request) {\n        $(NAME_UPPER) entity = $(NAME_UPPER).builder()\n                // TODO: mapear campos del request\n                .build();\n        return $(NAME_UPPER)Response.from($(NAME_LOWER)Repository.save(entity));\n    }\n\n    @Override\n    @Transactional(readOnly = true)\n    public $(NAME_UPPER)Response findById(UUID id) {\n        return $(NAME_UPPER)Response.from(getActive(id));\n    }\n\n    @Override\n    @Transactional(readOnly = true)\n    public List<$(NAME_UPPER)Response> findAll() {\n        return $(NAME_LOWER)Repository.findAll().stream()\n                .filter(e -> e.getDeletedAt() == null)\n                .map($(NAME_UPPER)Response::from)\n                .toList();\n    }\n\n    @Override\n    @Transactional\n    public $(NAME_UPPER)Response update(UUID id, Update$(NAME_UPPER)Request request) {\n        $(NAME_UPPER) entity = getActive(id);\n        // TODO: aplicar cambios del request\n        return $(NAME_UPPER)Response.from($(NAME_LOWER)Repository.save(entity));\n    }\n\n    @Override\n    @Transactional\n    public void softDelete(UUID id) {\n        $(NAME_UPPER) entity = getActive(id);\n        entity.setDeletedAt(LocalDateTime.now());\n        entity.setActive(false);\n        $(NAME_LOWER)Repository.save(entity);\n    }\n\n    private $(NAME_UPPER) getActive(UUID id) {\n        return $(NAME_LOWER)Repository.findById(id)\n                .filter(e -> e.getDeletedAt() == null)\n                .orElseThrow(() -> new IllegalArgumentException("$(NAME_UPPER) not found with id: " + id));\n    }\n}\n' > $(MODULE_PATH)/service/$(NAME_UPPER)ServiceImpl.java
	@echo "$(GREEN)  OK $(NAME_UPPER)Service.java + $(NAME_UPPER)ServiceImpl.java$(NC)"
	@echo "$(YELLOW)Generando Controller...$(NC)"
	@printf 'package $(BASE_PKG).$(NAME_LOWER).controller;\n\nimport $(BASE_PKG).$(NAME_LOWER).dto.Create$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.Update$(NAME_UPPER)Request;\nimport $(BASE_PKG).$(NAME_LOWER).dto.$(NAME_UPPER)Response;\nimport $(BASE_PKG).$(NAME_LOWER).service.$(NAME_UPPER)Service;\nimport jakarta.validation.Valid;\nimport lombok.RequiredArgsConstructor;\nimport org.springframework.http.HttpStatus;\nimport org.springframework.http.ResponseEntity;\nimport org.springframework.web.bind.annotation.*;\n\nimport java.util.List;\nimport java.util.UUID;\n\n@RestController\n@RequestMapping("/api/v1/$(NAME_LOWER)s")\n@RequiredArgsConstructor\npublic class $(NAME_UPPER)Controller {\n\n    private final $(NAME_UPPER)Service $(NAME_LOWER)Service;\n\n    @PostMapping\n    public ResponseEntity<$(NAME_UPPER)Response> create(@Valid @RequestBody Create$(NAME_UPPER)Request request) {\n        return ResponseEntity.status(HttpStatus.CREATED).body($(NAME_LOWER)Service.create(request));\n    }\n\n    @GetMapping("/{id}")\n    public ResponseEntity<$(NAME_UPPER)Response> findById(@PathVariable UUID id) {\n        return ResponseEntity.ok($(NAME_LOWER)Service.findById(id));\n    }\n\n    @GetMapping\n    public ResponseEntity<List<$(NAME_UPPER)Response>> findAll() {\n        return ResponseEntity.ok($(NAME_LOWER)Service.findAll());\n    }\n\n    @PatchMapping("/{id}")\n    public ResponseEntity<$(NAME_UPPER)Response> update(\n            @PathVariable UUID id,\n            @Valid @RequestBody Update$(NAME_UPPER)Request request\n    ) {\n        return ResponseEntity.ok($(NAME_LOWER)Service.update(id, request));\n    }\n\n    @DeleteMapping("/{id}")\n    public ResponseEntity<Void> delete(@PathVariable UUID id) {\n        $(NAME_LOWER)Service.softDelete(id);\n        return ResponseEntity.noContent().build();\n    }\n}\n' > $(MODULE_PATH)/controller/$(NAME_UPPER)Controller.java
	@echo "$(GREEN)  OK $(NAME_UPPER)Controller.java$(NC)"
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Modulo '$(NAME_UPPER)' generado exitosamente          $(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(BLUE)Archivos creados en $(MODULE_PATH)/:$(NC)"
	@echo "  entity/$(NAME_UPPER).java"
	@echo "  repository/$(NAME_UPPER)Repository.java"
	@echo "  service/$(NAME_UPPER)Service.java"
	@echo "  service/$(NAME_UPPER)ServiceImpl.java"
	@echo "  controller/$(NAME_UPPER)Controller.java"
	@echo "  dto/Create$(NAME_UPPER)Request.java"
	@echo "  dto/Update$(NAME_UPPER)Request.java"
	@echo "  dto/$(NAME_UPPER)Response.java"
	@echo ""
	@echo "$(YELLOW)Proximos pasos:$(NC)"
	@echo "  1. Agrega los campos del dominio en entity/$(NAME_UPPER).java"
	@echo "  2. Ajusta los DTOs con los campos necesarios"
	@echo "  3. Completa los TODO en ServiceImpl y Response"
	@echo "  4. Agrega queries en el Repository si los necesitas"
	@echo ""
