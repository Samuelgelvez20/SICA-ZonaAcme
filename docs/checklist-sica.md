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
- [x] Crear repo privado en GitHub, agregar a `trainingLeader` como colaborador.
- [x] `git flow init` (o estructura manual `main`/`develop`).
- [x] Proyecto Maven con `pom.xml` (dependencia driver JDBC PostgreSQL).
- [x] Paquetes por slice: `usuarios`, `personas`, `visitas`, `incidentes`, `reportes`, `auditoria`, `shared`, `config`, `ui`.
- [x] `docker-compose.yml` con el servicio de PostgreSQL.
- **Commit:** `chore: estructura base del proyecto Maven y docker-compose de postgres`

### HU-02 · Modelo de base de datos `[CRÍTICA]`
**Como** equipo del proyecto, **quiero** un modelo de datos claro y consistente, **para** que todos los módulos posteriores tengan dónde persistir su información.
- [x] Confirmar/ajustar el modelo de la sección 2.
- [x] `schema.sql` con tablas, PK/FK y constraints.
- [x] `data.sql`: roles base (Admin, Guarda, Funcionario), permisos mínimos (`crear_usuario`, `registrar_visita`, `generar_reporte`, `bloquear_persona` + los adicionales que se necesiten: `aprobar_visita`, `checkin_visita`, `checkout_visita`, `editar_persona`), usuarios de ejemplo por rol, empresas y personas de ejemplo.
- [x] Levantar el contenedor (`docker compose up -d`), correr los scripts, validar en DBeaver.
- [x] Diagrama Entidad-Relación (DBeaver ER Diagram o similar) para el README.
- **Commit:** `feat(db): agregar schema y data de PostgreSQL`

---

## Día 2 — Seguridad y trazabilidad [Prioridad global: CRÍTICA]

### HU-03 · Login y autenticación `[CRÍTICA]`
**Como** usuario del sistema (Guarda/Funcionario/Admin), **quiero** iniciar sesión con usuario y contraseña, **para** acceder solo a las funciones de mi rol.
- [x] Slice `usuarios`: entidades `Usuario`, `Rol`, `Permiso`.
- [x] Puerto `UsuarioRepository` + adaptador JDBC.
- [x] `ConexionPostgres` como **Singleton**.
- [x] `AutenticarUsuarioService` (valida credenciales, distingue éxito/fallo).
- [x] UI de login con `JOptionPane`.
- **Commit:** `feat(rbac): entidades de usuarios roles y permisos`, `feat(rbac): servicio de autenticación`

### HU-04 · Autorización por permisos `[CRÍTICA]`
**Como** sistema, **quiero** verificar que el rol del usuario tenga el permiso exacto antes de ejecutar cualquier acción crítica, **para** cumplir con RBAC y denegar accesos no autorizados con un mensaje claro.
- [x] `AutorizarAccionService`: recibe usuario + código de permiso, valida contra BD.
- [x] Excepción de dominio `AccesoDenegadoException` con mensaje claro para la UI.
- [x] Aplicar la verificación como punto de entrada obligatorio de cada caso de uso posterior (no opcional).
- **Commit:** `feat(rbac): validación de autorización por permiso`

### HU-05 · Bitácora de auditoría `[CRÍTICA]`
**Como** administrador, **quiero** que cada acción crítica quede registrada en una bitácora inmutable, **para** poder investigar incidentes y cumplir con trazabilidad.
- [x] Slice `auditoria`: entidad `BitacoraAuditoria`, puerto + adaptador JDBC.
- [x] `AuditoriaService.registrar(usuario, accion, entidad, detalle, resultado)`.
- [x] Integrarlo ya en login (éxito/fallo) y dejar el gancho listo para todos los servicios que vienen.
- **Commit:** `feat(auditoria): servicio de bitácora de auditoría`

---

## Día 3 — Personas, empresas y primer flujo de acceso [Prioridad global: ALTA]

### HU-06 · Gestión de personas y empresas `[ALTA]`
**Como** funcionario o admin, **quiero** registrar y consultar personas (trabajadores/invitados) y empresas, **para** tener la base de datos de quién puede circular por el complejo.
- [x] Slice `personas`: entidades `Persona`, `Empresa`.
- [x] CRUD protegido por permisos RBAC.
- [x] Cada operación dispara auditoría.
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
- [x] `RegistrarVisitaNoAnunciadaService` → estado `PENDIENTE_APROBACION`.
- [x] Si la persona no existe, permitir creación rápida desde la pantalla del Guarda.
- [x] Patrón **Strategy**: `ReglaValidacionIngreso` + `ValidacionIngresoNoAnunciado` (validación liviana).
- [x] `ValidacionIngresoException` en shared.
- [x] `VisitaFactory.crearNoAnunciada` (motivo="Ingreso no anunciado").
- **Commit:** `feat(visitas): HU-09 ingreso invitado no anunciado con Strategy`

### HU-10 · Notificación y aprobación en tiempo real `[ALTA]`
**Como** funcionario de empresa, **quiero** ver en mi pantalla las solicitudes pendientes apenas se generan y aprobarlas o rechazarlas, **para** que el Guarda reciba la respuesta sin demora.
- [x] Patrón **Observer**: `VisitaObserver`, `NotificadorVisitas` (puerto), `NotificadorVisitasEnMemoria` (adaptador con `CopyOnWriteArrayList` + `ExecutorService` 2 hilos).
- [x] Ejecución en hilo (`ExecutorService.newFixedThreadPool(2)`) para no bloquear la UI.
- [x] `AprobarORechazarVisitaService` (autorización `aprobar_visita`, auditoría, notificación async post-persistencia).
- [x] `VisitaNoDecidibleException` en shared (estados no decidibles / ya decididos).
- [x] `FuncionarioPendientesView` (Swing, `VisitaObserver`, carga inicial con `listarPendientesPorFuncionario`, updates async via `SwingUtilities.invokeLater`).
- [x] `PanelEsperaGuarda` (Swing, `VisitaObserver`, muestra decisión via `JOptionPane`, ofrece check-in inmediato).
    - **Evolución post-QA:** reemplazado por `PanelNotificacionesGuarda` (JFrame con JTable persistente + consulta directa a BD).
- [x] Cableado DI en `Main`: UNA sola instancia `NotificadorVisitasEnMemoria` compartida.
- [x] Conecta HU-09: `RegistrarVisitaNoAnunciadaService` notifica vía `NotificadorVisitas`.
- **Commit:** `feat(visitas): HU-10 Observer tiempo real + aprobaciones`

### HU-11 · Carnet olvidado `[ALTA]`
**Como** guarda de seguridad, **quiero** marcar el ingreso de un trabajador que olvidó su carnet, **para** que su funcionario apruebe un pase puntual para ese día.
- [x] `ValidacionIngresoPorOlvido` (2da implementación Strategy: solo TRABAJADOR, rechaza INVITADO).
- [x] `VisitaFactory.crearPorOlvido` → `PENDIENTE_APROBACION_OLVIDO`, motivo="Carnet olvidado".
- [x] `RegistrarIngresoPorOlvidoService` (NO crea persona, auto-rutea a funcionarioAnfitrionId/empresaId de la Persona).
- [x] `IngresoPorOlvidoView` (Swing: documento + botón, SwingWorker).
- [x] Reutiliza Observer de HU-10: NotificadorVisitas compartido, AprobarORechazarVisitaService, FuncionarioPendientesView sin cambios.
- **Commit:** `feat(visitas): HU-11 carnet olvidado (Strategy 2da implementación)`

### HU-12 · Salida olvidada (regularización automática) `[ALTA]`
**Como** sistema, **quiero** detectar si la última visita de una persona quedó abierta (`DENTRO`) al momento de un nuevo ingreso, **para** cerrarla automáticamente por auditoría sin bloquear el nuevo acceso.
- [x] Verificación previa a cualquier nuevo check-in (en `RegistrarCheckInService`).
- [x] Si aplica: cerrar la anterior como `CERRADA_POR_SISTEMA_SALIDA_OLVIDADA` + `fechaHoraSalida=now`.
- [x] Auditar `CIERRE_AUTOMATICO_SALIDA_OLVIDADA` como evento SEPARADO del `CHECKIN_VISITA`.
- [x] Caso normal (sin visita DENTRO previa) sin regresión.
- **Commit:** `refactor(visitas): HU-12 salida olvidada - regularización automática`

### HU-13 · Check-out normal `[ALTA]`
**Como** guarda de seguridad, **quiero** registrar la salida de una persona que sí se anuncia al retirarse, **para** cerrar correctamente su visita como `CERRADA`.
- [x] `RegistrarCheckOutService` (autoriza `checkout_visita`, busca visita DENTRO, cierra como CERRADA).
- [x] Búsqueda de la visita `DENTRO` activa y cierre con `fecha_hora_salida`.
- [x] Auditoría `CHECKOUT_VISITA` (éxito/fallo).
- [x] `CheckOutView` (Swing gemela a CheckInView: campo + botón, SwingWorker, JOptionPane).
- **Commit:** `feat(visitas): HU-13 check-out normal`

---

## Día 5 — Incidentes, reportes e integración [Prioridad global: MEDIA]

### HU-14 · Registro de incidentes y bloqueo de personas `[MEDIA]`
**Como** admin o funcionario con permiso, **quiero** registrar un incidente y poder bloquear a una persona, **para** impedir su ingreso futuro hasta que se resuelva.
- [x] Slice `incidentes`: entidad `Incidente`, `RegistrarIncidenteService`, `BloquearPersonaService` (permiso `bloquear_persona`).
- [x] El check-in debe impedir el ingreso si la persona está bloqueada, mostrando el motivo.
- [x] Auditoría en cada registro/bloqueo.
- **Commit:** `feat(incidentes): registro de incidentes y bloqueo de personas`

### HU-15 · Reportes `[MEDIA]`
**Como** admin, **quiero** ver quién está actualmente dentro del complejo y consultar la bitácora filtrada, **para** apoyar una evacuación o una investigación.
- [x] Reporte de personas `DENTRO` en tiempo real.
- [x] Consulta de bitácora filtrable por fecha/usuario/acción.
- [x] Uso explícito de **Streams/lambdas** (filtrar, agrupar, ordenar).
- **Commit:** `feat(reportes): reporte de personas dentro y consulta de bitácora`

### HU-16 · Integración de pantallas por rol `[MEDIA]`
**Como** cualquier usuario, **quiero** ver solo las opciones correspondientes a mi rol tras iniciar sesión, **para** tener una experiencia clara y consistente.
- [x] Pantalla principal condicionada por rol (Guarda / Funcionario / Admin).
- [x] Validaciones consistentes con `JOptionPane` (campos vacíos, numéricos, nulos).
- [x] Revisión cruzada: confirmar que TODAS las operaciones críticas llaman a `AuditoriaService`.
- **Commit:** `feat(ui): integración de pantallas por rol`, `fix(*): correcciones de validación`

---

## Día 6 — Documentación, QA y entrega [Prioridad global: ALTA para README, BAJA para cierre]

### HU-17 · README completo `[ALTA]`
**Como** evaluador del proyecto, **quiero** un README claro y completo, **para** entender, instalar y ejecutar el sistema sin ayuda externa.
- [x] Descripción del proyecto (problema + solución).
- [x] Modelo de la BD + diagrama ER.
- [x] Decisiones de diseño: dónde y por qué SOLID + los 5 patrones (Singleton, Observer, Repository, Strategy, Factory Method).
- [x] Nota explícita: por qué se usó Hexagonal + Vertical Slice+ (confirmado con el profesor como obligatorio) en vez de la mención a "MVC" del documento oficial de entregables.
- [x] Instalación y ejecución: JDK 25, Maven, `docker compose up -d` para Postgres, correr `schema.sql`/`data.sql`, compilar y ejecutar.
- [x] Guía de uso con credenciales de ejemplo por cada rol.
- **Commit:** `docs(readme): documentación completa del proyecto`

### HU-18 · QA manual `[BAJA]`
**Como** desarrollador, **quiero** probar los 4 flujos completos y el RBAC de punta a punta, **para** asegurar que todo lo pedido funciona antes de entregar.
- [x] Probar los 4 flujos (pre-registrado, no anunciado, olvido de carnet, salida olvidada) + check-out normal.
- [x] Probar RBAC: acción sin permiso → mensaje de denegación correcto.
- [x] Verificar que la bitácora registra TODO lo exigido por el documento.
- **Commit:** ajustes finales `fix(...)` si aparecen bugs.

### HU-19 · Cierre y entrega `[BAJA]`
**Como** desarrollador, **quiero** cerrar el repositorio ordenadamente, **para** entregar un proyecto profesional.
- [x] Merge final `develop` → `main`, tag `v1.0.0`.
- [x] Confirmar acceso de `trainingLeader` al repo.
- [x] Confirmar que `schema.sql`, `data.sql` y `README.md` están en la raíz.
- [x] Enviar el enlace del repositorio.

---

## Correcciones post-QA

Las siguientes modificaciones surgieron durante las pruebas manuales de QA
y corresponden a extensiones/correcciones posteriores al cierre funcional
inicial de las 19 historias. NO formaban parte del alcance original y
fueron integradas en `develop` después del merge `develop → main` con
tag `v1.0.0`.

### Extensión de UI para HU-09 — Vista de ingreso no anunciado

Se implementó `RegistroNoAnunciadoView.java` como interfaz Swing completa
para el flujo de ingreso no anunciado (extensión de UI de HU-09):

- Búsqueda de persona por documento (`personaRepository.buscarPorDocumento`).
- Manejo de persona existente: muestra nombre, tipo y empresa.
- Creación de datos para persona no registrada: formulario completo con
  nombre, tipo, foto URL y empresa ID.
- Selección de funcionario anfitrión mediante `JComboBox<Usuario>` filtrado
  por rol FUNCIONARIO y estado activo.
- Selección/ingreso de empresa visitada (campo obligatorio).
- Uso de `SwingWorker` para no bloquear la UI durante búsqueda y registro.
- Integración con `RegistrarVisitaNoAnunciadaService` existente.
- Integración en `PantallaPrincipal` como botón "Ingreso No Anunciado"
  visible para GUARDA y ADMIN.
- Solicitud enviada al funcionario con estado `PENDIENTE_APROBACION`.

**Corrección posterior:** Se reemplazó el campo manual de ID de funcionario
(`JTextField`) por un `JComboBox<Usuario>` que lista los funcionarios
activos del sistema, eliminando errores de selección manual y
auto-seleccionando el funcionario asociado a la persona buscada.

**Commits:** `066d9d8` (vista inicial), `08b70b8` (fix dropdown).

### Ingreso directo de trabajador con carnet

Se agregó un flujo post-QA para trabajadores con carnet válido que permite
ingreso directo sin pasar por aprobación:

```
TRABAJADOR + carnet válido → ingreso directo → estado DENTRO
```

Componentes implementados:

- `RegistrarIngresoTrabajadorService.java`: servicio que autoriza con
  permiso `checkin_visita`, busca persona por documento, valida que sea
  tipo TRABAJADOR (rechaza INVITADO), verifica bloqueo, regulariza
  salidas olvidadas y crea la visita directamente en estado `DENTRO`.
- `IngresoTrabajadorView.java`: interfaz Swing con campo documento + botón
  "Registrar Ingreso", usa `SwingWorker` y `JOptionPane`.
- `VisitaFactory.crearIngresoDirecto()`: factory method que crea visita
  con estado `DENTRO`, motivo "Ingreso directo con carnet" y
  `fechaHoraIngreso = LocalDateTime.now()`.
- Integración en el menú del GUARDA en `PantallaPrincipal` como botón
  "Ingreso de Trabajador".

Reglas de negocio:

- Autorización mediante `checkin_visita`.
- Solo/TRABAJADOR permitido; INVITADO rechazado con `ValidacionIngresoException`.
- Persona bloqueada → `PersonaBloqueadaException.conMotivo()`.
- Regularización automática de salida olvidada: cierra visita `DENTRO`
  anterior como `CERRADA_POR_SISTEMA_SALIDA_OLVIDADA` con auditoría
  separada.
- Auditoría completa: `INGRESO_DIRECTO_TRABAJADOR` (éxito/fallo),
  `INGRESO_DIRECTO_PERSONA_BLOQUEADA` (fallo).

Esta funcionalidad fue incorporada como extensión post-QA y no
corresponde a una HU adicional dentro de las 19 historias originales.

**Commit:** `d3ba7dc`.

### Correcciones de gestión de personas y empresas

Se realizaron correcciones y extensiones sobre la gestión existente de
personas y empresas (HU-06):

- **Filtro por empresa:** `JComboBox<Empresa>` en `PersonasView` que
  permite filtrar personas por empresa o mostrar todas.
- **Integración de `ListarPersonasPorEmpresaService`:** el filtro
  utiliza el servicio existente que verifica permiso `editar_persona`.
- **Edición de personas:** botón "Editar" en `PersonasView` que abre
  diálogo con todos los campos de la persona y actualiza mediante
  `ActualizarPersonaService`.
- **Corrección de cableado:** se ajustó el constructor de `PersonasView`
  para aceptar `ListarPersonasPorEmpresaService` como parámetro,
  resolviendo un bug de compilación pre-existente en `develop` donde
  `PantallaPrincipal` pasaba 6 argumentos a un constructor de 5.

Estas son correcciones/extensiones post-QA sobre la gestión existente.

**Commit:** `add285c`.

### Corrección: Panel de notificaciones del Guarda (modelo híbrido Observer + BD)

El mecanismo original de notificación al Guarda (popup `JOptionPane` emergente
vía `PanelEsperaGuarda`) solo funcionaba cuando Guarda y Funcionario corren
en el **mismo proceso JVM** (memoria compartida del `NotificadorVisitasEnMemoria`).
Si cada terminal es un proceso Java independiente, el Observer nunca disparaba.

**Solución:** Reemplazo de `PanelEsperaGuarda` (JPanel + popup) por
`PanelNotificacionesGuarda` (JFrame + JTable persistente):

- **Consulta directa a BD** (`VisitaRepository.listarPorGuarda(guardaId)`):
  funciona siempre, sin importar si corren en procesos separados, porque
  ambos apuntan a la misma PostgreSQL.
- **Observer en tiempo real** (mantiene `VisitaObserver`): actualiza la
  tabla automáticamente cuando Guarda y Funcionario comparten el mismo
  proceso JVM — complemento optimista, no la fuente de verdad.
- **Botón "Actualizar"** : re-consulta BD y refresca la tabla completa.
- **Botón "Check-in"** : habilitado solo para filas con estado `APROBADA`,
  reutiliza `RegistrarCheckInService`.
- **Carga automática** : al abrir la pantalla o al presionar Actualizar.
- **Botón "Mis Notificaciones"** agregado al menú del GUARDA en
  `PantallaPrincipal`.
- Nuevo método `listarPorGuarda` en `VisitaRepository` + JDBC
  (`ORDER BY creado_en DESC LIMIT 20`).

Archivos modificados: `VisitaRepository.java`, `VisitaRepositoryJdbc.java`,
`PanelNotificacionesGuarda.java` (nuevo), `PantallaPrincipal.java`.
Archivo eliminado: `PanelEsperaGuarda.java`.

**Commit:** `feat(visitas): panel notificaciones Guarda con modelo híbrido Observer+BD`

---

## BONUS (solo si sobra tiempo, sin sacrificar lo anterior)

**Técnico:**
- [ ] Explorar Spring Boot + WebSocket para la notificación en tiempo real.
- [ ] Pruebas unitarias con JUnit (autenticación, RBAC, flujo de visitas).
- [x] Patrón Strategy para reglas de validación de ingreso. Implementado en `domain/ReglaValidacionIngreso` (interfaz), `ValidacionIngresoNoAnunciado` (HU-09) y `ValidacionIngresoPorOlvido` (HU-11). Utilizado en `RegistrarVisitaNoAnunciadaService` y `RegistrarIngresoPorOlvidoService`.
- [x] Patrón Factory Method para creación de `Visita` según el flujo de origen. Implementado en `domain/VisitaFactory` con 4 métodos: `crearPreRegistrada` (HU-07), `crearNoAnunciada` (HU-09), `crearPorOlvido` (HU-11), `crearIngresoDirecto` (post-QA).
- [ ] Comparar JDBC puro vs Hibernate/JPA.
- [ ] Dockerizar también la aplicación (no solo la BD).

**Visual:**
- [ ] Look & Feel moderno en Swing (FlatLaf).
- [ ] Mostrar la foto del invitado (desde la URL) en la pantalla del Guarda.
- [ ] Dashboard de "personas dentro en este momento" con conteo en vivo.
- [ ] Exportar reportes a PDF o Excel.