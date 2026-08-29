# SICA — Sistema Integrado de Control de Acceso para Zona Acme

Sistema de escritorio en **Java SE 21** que digitaliza el control de acceso de un complejo empresarial con +30 empresas. Reemplaza libros de papel y radios por una aplicación con base de datos, autenticación, auditoría y notificación en tiempo real.

---

## Contenido

- [Problema y solución](#problema-y-solución)
- [Modelo de base de datos](#modelo-de-base-de-datos)
- [Arquitectura y patrones](#arquitectura-y-patrones)
- [Historias de usuario implementadas](#historias-de-usuario-implementadas)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Guía de uso](#guía-de-uso)
- [Credenciales de ejemplo](#credenciales-de-ejemplo)
- [Tecnologías](#tecnologías)

---

## Problema y solución

**Problema:** Zona Acme controla el acceso con libros de papel y radios, lo que genera inseguridad (no se sabe quién está dentro), filas, falta de trazabilidad y gestión reactiva de incidentes.

**Solución:** SICA digitaliza todo el flujo de entrada/salida:

| Funcionalidad | Descripción |
|---|---|
| Pre-registro | Un funcionario anticipa el ingreso de un invitado |
| Check-in | El guarda valida identidad y autoriza el ingreso |
| Notificación en tiempo real | El funcionario recibe y aprueba/rechaza solicitudes al instante |
| Carnet olvidado | Ingreso de trabajador que olvidó su identificación, con aprobación del funcionario |
| Salida olvidada | Cierre automático de visita abierta al registrar un nuevo ingreso |
| Incidentes y bloqueo | Registro de incidentes y bloqueo de acceso de personas |
| Auditoría | Cada acción crítica queda registrada con usuario, fecha y resultado |
| Reportes | Personas dentro del complejo en tiempo real + consulta de bitácora filtrada |

---

## Modelo de base de datos

```
roles(id, nombre)
permisos(id, codigo, descripcion)
rol_permisos(rol_id, permiso_id)
usuarios(id, username, password_hash, nombre, rol_id, activo, creado_en)
empresas(id, nombre, nit)
personas(id, nombre, documento, tipo, foto_url, empresa_id,
         funcionario_anfitrion_id, bloqueado, motivo_bloqueo)
visitas(id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
        fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
        estado, motivo, creado_en)
incidentes(id, persona_id, usuario_id, tipo, descripcion, fecha_hora)
bitacora_auditoria(id, usuario_id, accion, entidad, detalle,
                   fecha_hora, resultado)
```

**Estados de visita:**

```
APROBADA ──────────── Invitado pre-registrado aprobado por funcionario
PENDIENTE_APROBACION ── Ingreso no anunciado, esperando aprobación
PENDIENTE_APROBACION_OLVIDO ── Trabajador sin carnet, esperando aprobación
DENTRO ──────────────── Persona dentro del complejo
RECHAZADA ──────────── Solicitud rechazada por funcionario
CERRADA ────────────── Salida registrada normalmente
CERRADA_POR_SISTEMA_SALIDA_OLVIDADA ── Cerrada automáticamente al nuevo ingreso
```

---

## Arquitectura y patrones

### Arquitectura: Hexagonal + Vertical Slice+

Cada módulo de negocio (`usuarios`, `personas`, `visitas`, `incidentes`, `reportes`, `auditoria`) está aislado en su propio paquete con tres capas:

```
com.acme.sica/
├── config/              ── Singleton: ConexionPostgres
├── shared/              ── Excepciones de dominio compartidas
├── usuarios/            ── Autenticación, RBAC, gestión de usuarios
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── personas/            ── CRUD personas y empresas
├── visitas/             ── Flujos de acceso (check-in, check-out, pre-registro, etc.)
├── incidentes/          ── Registro de incidentes y bloqueo
├── reportes/            ── Reportes de personas dentro y bitácora
└── auditoria/           ── Bitácora inmutable
```

### 5 Patrones de diseño

| Patróon | Ubicación | Descripción |
|---------|-----------|-------------|
| **Singleton** | `config/ConexionPostgres` | Una sola conexión a PostgreSQL compartida por todos los repositorios |
| **Repository** | `*Repository` + `*RepositoryJdbc` | Puertos de dominio (interfaces) + adaptadores JDBC (implementaciones). Separa dominio de persistencia |
| **Observer** | `VisitaObserver` + `NotificadorVisitasEnMemoria` | Notificación asíncrona cuando un guarda registra una solicitud pendiente. Usa `CopyOnWriteArrayList` + `ExecutorService` (2 hilos) |
| **Strategy** | `ReglaValidacionIngreso` + `ValidacionIngresoNoAnunciado` + `ValidacionIngresoPorOlvido` | Cada flujo de ingreso tiene su propia regla de validación. El servicio decide cuál usar |
| **Factory Method** | `VisitaFactory` | Cada tipo de visita (pre-registrada, no anunciada, por olvido) se crea con un método estático específico |

### Principios SOLID aplicados

- **SRP (Single Responsibility):** Cada servicio hace una sola cosa (`RegistrarCheckInService` solo hace check-in)
- **OCP (Open/Closed):** Nuevos flujos de ingreso se agregan implementando `ReglaValidacionIngreso` sin modificar el existente
- **LSP (Liskov Substitution):** Las implementaciones de `Repository` son intercambiables (JDBC → JPA futuro)
- **ISP (Interface Segregation):** Cada repository expone solo los métodos que su dominio necesita
- **DIP (Dependency Inversion):** `Main.java` inyecta dependencias manualmente; los servicios dependen de interfaces, no de implementaciones

### Por qué Hexagonal + Vertical Slice+ en vez de MVC

El documento oficial de entregables menciona "MVC" como plantilla genérica, pero fue confirmado con el profesor que la arquitectura correcta para este proyecto es **Hexagonal + Vertical Slice+**. MVC separa por capas técnicas (Model/View/Controller), lo que no escala bien cuando hay múltiples flujos de negocio independientes. Vertical Slice+ organiza por caso de uso, permitiendo que cada feature tenga su propio dominio, aplicación e infraestructura aislados. Esto facilita el desarrollo incremental, la prueba aislada y la comprensión del código.

---

## Historias de usuario implementadas

### Día 1 — Cimientos

| HU | Nombre | Archivos clave |
|----|--------|---------------|
| HU-01 | Estructura base del proyecto | `pom.xml`, `docker-compose.yml` |
| HU-02 | Modelo de base de datos | `schema.sql`, `data.sql` |

### Día 2 — Seguridad y trazabilidad

| HU | Nombre | Archivos clave |
|----|--------|---------------|
| HU-03 | Login y autenticación | `AutenticarUsuarioService`, `LoginView`, `ConexionPostgres` |
| HU-04 | Autorización por permisos | `AutorizarAccionService`, `AccesoDenegadoException` |
| HU-05 | Bitácora de auditoría | `AuditoriaService`, `BitacoraAuditoria`, `BitacoraRepositoryJdbc` |

### Día 3 — Personas, empresas y primer acceso

| HU | Nombre | Archivos clave |
|----|--------|---------------|
| HU-06 | Gestión de personas y empresas | `CrearPersonaService`, `CrearEmpresaService`, `PersonasView`, `EmpresasView` |
| HU-07 | Pre-registro de invitado | `PreRegistrarInvitadoService`, `VisitaFactory.crearPreRegistrada` |
| HU-08 | Check-in del invitado pre-registrado | `RegistrarCheckInService`, `CheckInView` |

### Día 4 — Flujos en tiempo real y regularización

| HU | Nombre | Archivos clave |
|----|--------|---------------|
| HU-09 | Ingreso de invitado no anunciado | `RegistrarVisitaNoAnunciadaService`, `ValidacionIngresoNoAnunciado` (Strategy) |
| HU-10 | Notificación y aprobación en tiempo real | `VisitaObserver`, `NotificadorVisitasEnMemoria` (Observer), `FuncionarioPendientesView` |
| HU-11 | Carnet olvidado | `RegistrarIngresoPorOlvidoService`, `ValidacionIngresoPorOlvido` (Strategy 2) |
| HU-12 | Salida olvidada (regularización automática) | `RegistrarCheckInService` (verificación previa), `VisitaNoDecidibleException` |
| HU-13 | Check-out normal | `RegistrarCheckOutService`, `CheckOutView` |

### Día 5 — Incidentes, reportes e integración

| HU | Nombre | Archivos clave |
|----|--------|---------------|
| HU-14 | Registro de incidentes y bloqueo | `RegistrarIncidenteService`, `BloquearPersonaService`, `PersonaBloqueadaException` |
| HU-15 | Reportes | `GenerarReporteVisitasDentroService`, `GenerarReporteBitacoraService`, `FiltrosBitacora` |
| HU-16 | Integración de pantallas por rol | `PantallaPrincipal`, `PersonasView`, `EmpresasView`, `ReporteVisitasDentroView`, `ReporteBitacoraView` |

### Día 6 — Documentación y entrega

| HU | Nombre |
|----|--------|
| HU-17 | README completo (este documento) |
| HU-18 | QA manual |
| HU-19 | Cierre y entrega |

---

## Instalación y ejecución

### Requisitos previos

| Requisito | Versión mínima | Verificación |
|-----------|----------------|-------------|
| Java (JDK) | 21+ | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| Docker | 20.10+ | `docker --version` |

### Paso 1: Levantar PostgreSQL

```bash
docker compose up -d
```

Verificar que el contenedor está corriendo:

```bash
docker ps | grep sica-postgres
```

### Paso 2: Crear la base de datos

Ejecutar los scripts SQL en orden (una sola conexión a PostgreSQL):

```bash
psql -h localhost -U sica_user -d sica -f src/main/resources/db/schema.sql
psql -h localhost -U sica_user -d sica -f src/main/resources/db/data.sql
```

O usar DBeaver: conectar a `localhost:5432`, usuario `sica_user`, contraseña `sica_pass`, base `sica`, y ejecutar los scripts.

### Paso 3: Compilar

```bash
mvn compile -q
```

### Paso 4: Ejecutar

```bash
mvn -q exec:java -Dexec.mainClass="com.acme.sica.Main"
```

O desde IntelliJ/VS Code: ejecutar `Main.java`.

---

## Guía de uso

### Flujo 1: Pre-registro de invitado (Funcionario → Guarda)

```
1. Login como funcionario1 → PantallaPrincipal (Funcionario)
2. Clic "Pre-registrar Invitado"
3. Ingresar: Persona ID, Empresa Visitada ID, Fecha
4. Invitado queda en estado APROBADA
5. Login como guarda1 → Check-in
6. Ingresar documento del invitado → Ingreso exitoso
```

### Flujo 2: Ingreso no anunciado (Guarda → Funcionario)

```
1. Login como guarda1 → PantallaPrincipal (Guarda)
2. Clic "Check-in"
3. Ingresar documento del invitado que no está pre-registrado
4. Sistema crea la visita en PENDIENTE_APROBACION
5. Funcionario recibe notificación en tiempo real
6. Funcionario aprueba o rechaza desde "Solicitudes Pendientes"
```

### Flujo 3: Carnet olvidado (Guarda → Funcionario)

```
1. Login como guarda1 → PantallaPrincipal (Guarda)
2. Clic "Ingreso por Olvido"
3. Ingresar documento del trabajador
4. Sistema crea la visita en PENDIENTE_APROBACION_OLVIDO
5. Funcionario aprueba o rechaza desde "Solicitudes Pendientes"
```

### Flujo 4: Salida olvidada (Automático)

```
1. Persona con visita DENTRO intenta hacer un nuevo check-in
2. Sistema detecta la visita abierta y la cierra automáticamente
   como CERRADA_POR_SISTEMA_SALIDA_OLVIDADA
3. Nuevo check-in se procesa normalmente
```

### RBAC por rol

| Opción | ADMIN | FUNCIONARIO | GUARDA |
|--------|-------|-------------|--------|
| Check-in | ✅ | — | ✅ |
| Check-out | ✅ | — | ✅ |
| Ingreso por Olvido | ✅ | — | ✅ |
| Solicitudes Pendientes | ✅ | ✅ | — |
| Pre-registrar Invitado | ✅ | ✅ | — |
| Registrar Incidente | ✅ | ✅ | — |
| Bloquear Persona | ✅ | ✅ | — |
| Gestionar Personas | ✅ | — | — |
| Gestionar Empresas | ✅ | — | — |
| Reporte Personas Dentro | ✅ | ✅ | — |
| Reporte Bitácora | ✅ | ✅ | — |

---

## Credenciales de ejemplo

Todos los usuarios tienen contraseña `1234` (SHA-256).

| Username | Contraseña | Rol | Nombre |
|----------|-----------|-----|--------|
| `admin` | `1234` | ADMIN | Administrador General |
| `guarda1` | `1234` | GUARDA | Carlos Pérez |
| `funcionario1` | `1234` | FUNCIONARIO | Laura Gómez |

**Personas de ejemplo:**

| Documento | Nombre | Tipo | Empresa |
|-----------|--------|------|---------|
| `1001234567` | Juan Torres | TRABAJADOR | TechNova S.A.S. |
| `1009876543` | María Rodríguez | INVITADO | — |

**Empresas de ejemplo:**

| Nombre | NIT |
|--------|-----|
| TechNova S.A.S. | 900123456-1 |
| Acme Logistics | 900654321-2 |

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 (JDK Temurin 25) |
| Build | Maven 3.8+ |
| UI | Java Swing |
| Base de datos | PostgreSQL 16 (Docker) |
| Conector JDBC | PostgreSQL JDBC 42.7.4 |
| Contenedor | Docker Compose |
| IDE recomendado | IntelliJ IDEA / VS Code |

---

## Licencia

Proyecto académico — Universidad (Zona Acme).
