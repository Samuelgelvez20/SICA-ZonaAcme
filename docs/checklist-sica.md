# Checklist — SICA (Sistema Integrado de Control de Acceso para Zona Acme)

> Plan de trabajo para 6 días × 6 horas = **36 horas**, en solitario, organizado por día en formato de historias de usuario.
> Cada historia trae una etiqueta de prioridad **[CRÍTICA / ALTA / MEDIA / BAJA]**. Si un día se atrasa, sacrifica primero las historias de menor prioridad de ese mismo día, nunca las críticas.

---

## 0. Descripción del proyecto

Zona Acme es un complejo empresarial con más de 30 empresas que actualmente controla el acceso con libros de papel y radios, lo que genera inseguridad (no se sabe quién está dentro), filas, falta de trazabilidad y gestión reactiva de incidentes. SICA es la aplicación de escritorio en Java que digitaliza y asegura todo el flujo de entrada/salida: registro de invitados y trabajadores, aprobación en tiempo real por parte del funcionario anfitrión, bitácora de auditoría inmutable y control de acceso basado en roles (RBAC) configurable desde base de datos.

## 1. Decisiones técnicas (confirmadas)

| Decisión | Elección |
|---|---|
| Tipo de app | Java SE puro, Swing (pantallas Guarda/Funcionario/Admin) + `JOptionPane` para diálogos puntuales. |
| Motor de BD | **PostgreSQL en Docker** (`docker-compose.yml` con un solo servicio), visualizado con **DBeaver**. |
| Acceso a datos | JDBC puro, envuelto en repositorios (puertos/adaptadores). |
| Build | Maven (single module, paquetes por *feature*). |
| Java | JDK 25 (Temurin), `maven.compiler.release=21`. |
| Concurrencia / tiempo real | Hilos + patrón **Observer**. |
| Spring Boot | No se usa en el alcance obligatorio — queda como ítem de bonus para explorar si sobra tiempo. |
| Arquitectura | Hexagonal aplicando Vertical Slice+ (paquetes por feature, cada uno con `domain` / `application` / `infrastructure`). |
| Patrones obligatorios | Mínimo **5** (documento oficial, actualizado desde el borrador que pedía 2): **Singleton** (conexión a Postgres, ✅ implementado en HU-03), **Observer** (notificación tiempo real, HU-10), **Repository** (puertos/adaptadores ya en uso en todos los slices desde HU-03), **Strategy** (reglas de validación de ingreso, HU-09/HU-11), **Factory Method** (creación de `Visita` según el flujo de origen, HU-07/09/11). |
| Arquitectura (confirmado con el profesor) | **Hexagonal + Vertical Slice+ es obligatorio**, según indicación directa del profesor. El documento oficial menciona "MVC" en la sección de entregables, pero es texto de plantilla genérico — no aplica a este proyecto. Se documenta esta decisión en el README (HU-17). |
| Lambdas / Stream API | Obligatorio en reportes, filtros de bitácora, listados de pendientes, validaciones en cadena, etc. — se marca explícitamente en cada historia donde aplica. |
| Repo | Privado en GitHub, colaborador: `trainingLeader`. |

## 2. Modelo de datos (mínimo razonable, se ajusta el Día 1)

```
roles(id, nombre)
permisos(id, codigo, descripcion)              -- incluye mínimo: crear_usuario, registrar_visita, generar_reporte, bloquear_persona
rol_permisos(rol_id, permiso_id)
usuarios(id, username, password_hash, nombre, rol_id, activo)
empresas(id, nombre, nit)
personas(id, nombre, documento, tipo[TRABAJADOR|INVITADO], foto_url,
         empresa_id, funcionario_anfitrion_id, bloqueado)
visitas(id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
        fecha_hora_ingreso, fecha_hora_salida, estado, motivo)
incidentes(id, persona_id, usuario_id, tipo, descripcion, fecha_hora)
bitacora_auditoria(id, usuario_id, accion, entidad, detalle, fecha_hora, resultado)
```

Estados de `visitas`: `APROBADA`, `PENDIENTE_APROBACION`, `PENDIENTE_APROBACION_OLVIDO`, `DENTRO`, `RECHAZADA`, `CERRADA`, `CERRADA_POR_SISTEMA_SALIDA_OLVIDADA`.

## 3. Git Flow y commits (transversal — todos los días)

- Ramas: `main` (entrega estable) · `develop` (integración) · `feature/<slice>` (una por módulo).
- Al terminar cada historia: commit → merge de `feature/*` a `develop`.
- Al final del proyecto (Día 6): merge `develop` → `main`, tag `v1.0.0`.
- Conventional Commits: `feat(rbac): ...`, `fix(visitas): ...`, `docs(readme): ...`, `chore(config): ...`, `refactor(...): ...`. Mínimo 3-4 commits por día de código.

---

## Día 1 — Cimientos del proyecto [Prioridad global: CRÍTICA]

### HU-01 · Estructura base del proyecto `[CRÍTICA]`
**Como** desarrollador, **quiero** tener el repositorio y el proyecto Maven configurados con Git Flow, **para** empezar a desarrollar sobre una base ordenada.
- [ ] Crear repo privado en GitHub, agregar a `trainingLeader` como colaborador.
- [ ] `git flow init` (o estructura manual `main`/`develop`).
- [ ] Proyecto Maven con `pom.xml` (dependencia driver JDBC PostgreSQL).
- [ ] Paquetes por slice: `usuarios`, `personas`, `visitas`, `incidentes`, `reportes`, `auditoria`, `shared`, `config`, `ui`.
- [ ] `docker-compose.yml` con el servicio de PostgreSQL.
- **Commit:** `chore: estructura base del proyecto Maven y docker-compose de postgres`

### HU-02 · Modelo de base de datos `[CRÍTICA]`
**Como** equipo del proyecto, **quiero** un modelo de datos claro y consistente, **para** que todos los módulos posteriores tengan dónde persistir su información.
- [ ] Confirmar/ajustar el modelo de la sección 2.
- [ ] `schema.sql` con tablas, PK/FK y constraints.
- [ ] `data.sql`: roles base (Admin, Guarda, Funcionario), permisos mínimos (`crear_usuario`, `registrar_visita`, `generar_reporte`, `bloquear_persona` + los adicionales que se necesiten: `aprobar_visita`, `checkin_visita`, `checkout_visita`, `editar_persona`), usuarios de ejemplo por rol, empresas y personas de ejemplo.
- [ ] Levantar el contenedor (`docker compose up -d`), correr los scripts, validar en DBeaver.
- [ ] Diagrama Entidad-Relación (DBeaver ER Diagram o similar) para el README.
- **Commit:** `feat(db): agregar schema y data de PostgreSQL`

---

## Día 2 — Seguridad y trazabilidad [Prioridad global: CRÍTICA]

### HU-03 · Login y autenticación `[CRÍTICA]`
**Como** usuario del sistema (Guarda/Funcionario/Admin), **quiero** iniciar sesión con usuario y contraseña, **para** acceder solo a las funciones de mi rol.
- [ ] Slice `usuarios`: entidades `Usuario`, `Rol`, `Permiso`.
- [ ] Puerto `UsuarioRepository` + adaptador JDBC.
- [ ] `ConexionPostgres` como **Singleton**.
- [ ] `AutenticarUsuarioService` (valida credenciales, distingue éxito/fallo).
- [ ] UI de login con `JOptionPane`.
- **Commit:** `feat(rbac): entidades de usuarios roles y permisos`, `feat(rbac): servicio de autenticación`

### HU-04 · Autorización por permisos `[CRÍTICA]`
**Como** sistema, **quiero** verificar que el rol del usuario tenga el permiso exacto antes de ejecutar cualquier acción crítica, **para** cumplir con RBAC y denegar accesos no autorizados con un mensaje claro.
- [ ] `AutorizarAccionService`: recibe usuario + código de permiso, valida contra BD.
- [ ] Excepción de dominio `AccesoDenegadoException` con mensaje claro para la UI.
- [ ] Aplicar la verificación como punto de entrada obligatorio de cada caso de uso posterior (no opcional).
- **Commit:** `feat(rbac): validación de autorización por permiso`

### HU-05 · Bitácora de auditoría `[CRÍTICA]`
**Como** administrador, **quiero** que cada acción crítica quede registrada en una bitácora inmutable, **para** poder investigar incidentes y cumplir con trazabilidad.
- [ ] Slice `auditoria`: entidad `BitacoraAuditoria`, puerto + adaptador JDBC.
- [ ] `AuditoriaService.registrar(usuario, accion, entidad, detalle, resultado)`.
- [ ] Integrarlo ya en login (éxito/fallo) y dejar el gancho listo para todos los servicios que vienen.
- **Commit:** `feat(auditoria): servicio de bitácora de auditoría`

---

## Día 3 — Personas, empresas y primer flujo de acceso [Prioridad global: ALTA]

### HU-06 · Gestión de personas y empresas `[ALTA]`
**Como** funcionario o admin, **quiero** registrar y consultar personas (trabajadores/invitados) y empresas, **para** tener la base de datos de quién puede circular por el complejo.
- [ ] Slice `personas`: entidades `Persona`, `Empresa`.
- [ ] CRUD protegido por permisos RBAC.
- [ ] Cada operación dispara auditoría.
- **Commit:** `feat(personas): CRUD de personas y empresas con RBAC y auditoría`

### HU-07 · Pre-registro de invitado `[ALTA]`
**Como** funcionario de empresa, **quiero** pre-registrar a un invitado con fecha/hora de visita, **para** que su ingreso quede aprobado de antemano.
- [x] Slice `visitas`: entidad `Visita`.
- [x] `PreRegistrarInvitadoService` → estado `APROBADA`.
- **Commit:** `feat(visitas): pre-registro de invitados`

### HU-08 · Check-in del invitado pre-registrado `[ALTA]`
**Como** guarda de seguridad, **quiero** buscar al invitado por documento y ver su foto, empresa y autorización, **para** dejarlo ingresar rápidamente.
- [x] `RegistrarCheckInService` (estado → `DENTRO`).
- [x] UI de búsqueda/check-in para el Guarda (`CheckInView`).
- [x] Auditoría del check-in.
- **Commit:** `feat(visitas): check-in de invitado aprobado`

---

## Día 4 — Flujos en tiempo real y regularización [Prioridad global: ALTA]

### HU-09 · Ingreso de invitado no anunciado `[ALTA]`
**Como** guarda de seguridad, **quiero** registrar a un invitado que llega sin cita (creando su Persona si no existe), **para** que el sistema le pida aprobación al funcionario correspondiente.
- [ ] `RegistrarVisitaNoAnunciadaService` → estado `PENDIENTE_APROBACION`.
- [ ] Si la persona no existe, permitir creación rápida desde la pantalla del Guarda.
- **Commit:** `feat(visitas): registro de invitado no anunciado`

### HU-10 · Notificación y aprobación en tiempo real `[ALTA]`
**Como** funcionario de empresa, **quiero** ver en mi pantalla las solicitudes pendientes apenas se generan y aprobarlas o rechazarlas, **para** que el Guarda reciba la respuesta sin demora.
- [ ] Patrón **Observer**: `VisitaObserver`, `VisitaNotificador` (sujeto).
- [ ] Ejecución en hilo (`Thread`/`ExecutorService`) para no bloquear la UI.
- [ ] `AprobarORechazarVisitaService`.
- [ ] Pantalla del Funcionario (lista de pendientes) y pantalla del Guarda (se refresca al notificarse).
- **Commit:** `feat(visitas): flujo de aprobación en tiempo real con observer`

### HU-11 · Carnet olvidado `[ALTA]`
**Como** guarda de seguridad, **quiero** marcar el ingreso de un trabajador que olvidó su carnet, **para** que su funcionario apruebe un pase puntual para ese día.
- [ ] `RegistrarIngresoPorOlvidoService` → estado `PENDIENTE_APROBACION_OLVIDO`.
- [ ] Reutiliza el mismo mecanismo Observer de HU-10.
- **Commit:** `feat(visitas): ingreso por carnet olvidado`

### HU-12 · Salida olvidada (regularización automática) `[ALTA]`
**Como** sistema, **quiero** detectar si la última visita de una persona quedó abierta (`DENTRO`) al momento de un nuevo ingreso, **para** cerrarla automáticamente por auditoría sin bloquear el nuevo acceso.
- [ ] Verificación previa a cualquier nuevo check-in.
- [ ] Si aplica: cerrar la anterior como `CERRADA_POR_SISTEMA_SALIDA_OLVIDADA` + crear la nueva visita.
- [ ] Auditar ambos eventos.
- **Commit:** `feat(visitas): regularización automática de salida olvidada`

### HU-13 · Check-out normal `[ALTA]`
**Como** guarda de seguridad, **quiero** registrar la salida de una persona que sí se anuncia al retirarse, **para** cerrar correctamente su visita como `CERRADA`.
- [ ] `RegistrarCheckOutService`.
- [ ] Búsqueda de la visita `DENTRO` activa de esa persona y cierre con `fecha_hora_salida`.
- [ ] Auditoría del check-out.
- **Commit:** `feat(visitas): check-out normal de visita`

---

## Día 5 — Incidentes, reportes e integración [Prioridad global: MEDIA]

### HU-14 · Registro de incidentes y bloqueo de personas `[MEDIA]`
**Como** admin o funcionario con permiso, **quiero** registrar un incidente y poder bloquear a una persona, **para** impedir su ingreso futuro hasta que se resuelva.
- [ ] Slice `incidentes`: entidad `Incidente`, `RegistrarIncidenteService`, `BloquearPersonaService` (permiso `bloquear_persona`).
- [ ] El check-in debe impedir el ingreso si la persona está bloqueada, mostrando el motivo.
- [ ] Auditoría en cada registro/bloqueo.
- **Commit:** `feat(incidentes): registro de incidentes y bloqueo de personas`

### HU-15 · Reportes `[MEDIA]`
**Como** admin, **quiero** ver quién está actualmente dentro del complejo y consultar la bitácora filtrada, **para** apoyar una evacuación o una investigación.
- [ ] Reporte de personas `DENTRO` en tiempo real.
- [ ] Consulta de bitácora filtrable por fecha/usuario/acción.
- [ ] Uso explícito de **Streams/lambdas** (filtrar, agrupar, ordenar).
- **Commit:** `feat(reportes): reporte de personas dentro y consulta de bitácora`

### HU-16 · Integración de pantallas por rol `[MEDIA]`
**Como** cualquier usuario, **quiero** ver solo las opciones correspondientes a mi rol tras iniciar sesión, **para** tener una experiencia clara y consistente.
- [ ] Pantalla principal condicionada por rol (Guarda / Funcionario / Admin).
- [ ] Validaciones consistentes con `JOptionPane` (campos vacíos, numéricos, nulos).
- [ ] Revisión cruzada: confirmar que TODAS las operaciones críticas llaman a `AuditoriaService`.
- **Commit:** `feat(ui): integración de pantallas por rol`, `fix(*): correcciones de validación`

---

## Día 6 — Documentación, QA y entrega [Prioridad global: ALTA para README, BAJA para cierre]

### HU-17 · README completo `[ALTA]`
**Como** evaluador del proyecto, **quiero** un README claro y completo, **para** entender, instalar y ejecutar el sistema sin ayuda externa.
- [ ] Descripción del proyecto (problema + solución).
- [ ] Modelo de la BD + diagrama ER.
- [ ] Decisiones de diseño: dónde y por qué SOLID + los 5 patrones (Singleton, Observer, Repository, Strategy, Factory Method).
- [ ] Nota explícita: por qué se usó Hexagonal + Vertical Slice+ (confirmado con el profesor como obligatorio) en vez de la mención a "MVC" del documento oficial de entregables.
- [ ] Instalación y ejecución: JDK 25, Maven, `docker compose up -d` para Postgres, correr `schema.sql`/`data.sql`, compilar y ejecutar.
- [ ] Guía de uso con credenciales de ejemplo por cada rol.
- **Commit:** `docs(readme): documentación completa del proyecto`

### HU-18 · QA manual `[BAJA]`
**Como** desarrollador, **quiero** probar los 4 flujos completos y el RBAC de punta a punta, **para** asegurar que todo lo pedido funciona antes de entregar.
- [ ] Probar los 4 flujos (pre-registrado, no anunciado, olvido de carnet, salida olvidada) + check-out normal.
- [ ] Probar RBAC: acción sin permiso → mensaje de denegación correcto.
- [ ] Verificar que la bitácora registra TODO lo exigido por el documento.
- **Commit:** ajustes finales `fix(...)` si aparecen bugs.

### HU-19 · Cierre y entrega `[BAJA]`
**Como** desarrollador, **quiero** cerrar el repositorio ordenadamente, **para** entregar un proyecto profesional.
- [ ] Merge final `develop` → `main`, tag `v1.0.0`.
- [ ] Confirmar acceso de `trainingLeader` al repo.
- [ ] Confirmar que `schema.sql`, `data.sql` y `README.md` están en la raíz.
- [ ] Enviar el enlace del repositorio.

---

## BONUS (solo si sobra tiempo, sin sacrificar lo anterior)

**Técnico:**
- [ ] Explorar Spring Boot + WebSocket para la notificación en tiempo real.
- [ ] Pruebas unitarias con JUnit (autenticación, RBAC, flujo de visitas).
- [ ] Patrón Strategy para reglas de validación de ingreso.
- [ ] Patrón Factory Method para creación de `Visita` según el flujo de origen.
- [ ] Comparar JDBC puro vs Hibernate/JPA.
- [ ] Dockerizar también la aplicación (no solo la BD).

**Visual:**
- [ ] Look & Feel moderno en Swing (FlatLaf).
- [ ] Mostrar la foto del invitado (desde la URL) en la pantalla del Guarda.
- [ ] Dashboard de "personas dentro en este momento" con conteo en vivo.
- [ ] Exportar reportes a PDF o Excel.