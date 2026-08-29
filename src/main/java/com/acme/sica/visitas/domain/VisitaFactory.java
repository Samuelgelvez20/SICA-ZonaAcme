package com.acme.sica.visitas.domain;

import java.time.LocalDateTime;

/**
 * Factory para la creación de objetos {@link Visita} según el flujo de origen.
 *
 * <p>Este es el patrón <b>Factory Method</b> (uno de los 5 patrones obligatorios
 * del proyecto). Encapsula la lógica de construcción de una {@code Visita} en
 * sus distintos estados iniciales, evitando que los servicios de aplicación
 * conozcan los detalles de qué campos deben ir a null o a qué valor por defecto
 * según el caso de uso (pre-registro, no anunciado, olvido de carnet).
 *
 * <p>Justificación de ubicarla en {@code domain}: una factory de un objeto de
 * dominio puede vivir en {@code domain} sin romper la arquitectura hexagonal,
 * ya que no depende de infraestructura (BD, frameworks, UI). Solo crea objetos
 * puros del dominio. Moverla a {@code application} añadiría una capa innecesaria
 * sin beneficio, porque los servicios de aplicación ya dependen del dominio.
 */
public final class VisitaFactory {

    private VisitaFactory() {
    }

    /**
     * Crea una visita pre-registrada por un funcionario.
     *
     * @param personaId           ID de la persona invitada (ya registrada)
     * @param funcionarioId       ID del funcionario que hace el pre-registro
     * @param empresaVisitadaId   ID de la empresa que recibe la visita
     * @param fechaHoraProgramada Fecha/hora planeada de la visita (puede ser null)
     * @return Visita en estado {@link EstadoVisita#APROBADA}, con
     * {@code guardaId = null}, {@code fechaHoraIngreso = null},
     * {@code fechaHoraSalida = null}, {@code motivo = null}
     */
    public static Visita crearPreRegistrada(Long personaId, Long funcionarioId,
                                            Long empresaVisitadaId,
                                            LocalDateTime fechaHoraProgramada) {
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setEmpresaVisitadaId(empresaVisitadaId);
        visita.setFechaHoraProgramada(fechaHoraProgramada);
        visita.setEstado(EstadoVisita.APROBADA);
        // guardaId, fechaHoraIngreso, fechaHoraSalida, motivo quedan null
        return visita;
    }

    /**
     * Crea una visita por ingreso no anunciado (invitado sin cita previa).
     *
     * @param personaId         ID de la persona (ya registrada o reci\u00e9n creada)
     * @param guardaId          ID del guarda que registra el ingreso
     * @param funcionarioId     ID del funcionario anfitri\u00f3n de la empresa visitada
     * @param empresaVisitadaId ID de la empresa que recibe la visita
     * @return Visita en estado {@link EstadoVisita#PENDIENTE_APROBACION},
     * {@code fechaHoraProgramada = null}, {@code fechaHoraIngreso = null},
     * {@code fechaHoraSalida = null}, {@code motivo = "Ingreso no anunciado"}
     */
    public static Visita crearNoAnunciada(Long personaId, Long guardaId,
                                          Long funcionarioId, Long empresaVisitadaId) {
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setGuardaId(guardaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setEmpresaVisitadaId(empresaVisitadaId);
        visita.setFechaHoraProgramada(null);
        visita.setFechaHoraIngreso(null);
        visita.setEstado(EstadoVisita.PENDIENTE_APROBACION);
        visita.setMotivo("Ingreso no anunciado");
        return visita;
    }

    /**
     * Crea una visita por carnet olvidado (trabajador sin documento f\u00edsico).
     *
     * @param personaId         ID del trabajador (ya registrado)
     * @param guardaId          ID del guarda que registra el ingreso
     * @param funcionarioId     ID del funcionario anfitri\u00f3n (tomado de persona.getFuncionarioAnfitrionId)
     * @param empresaVisitadaId ID de la empresa del trabajador (tomado de persona.getEmpresaId)
     * @return Visita en estado {@link EstadoVisita#PENDIENTE_APROBACION_OLVIDO},
     * {@code fechaHoraProgramada = null}, {@code fechaHoraIngreso = null},
     * {@code fechaHoraSalida = null}, {@code motivo = "Carnet olvidado"}
     */
    public static Visita crearPorOlvido(Long personaId, Long guardaId,
                                        Long funcionarioId, Long empresaVisitadaId) {
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setGuardaId(guardaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setEmpresaVisitadaId(empresaVisitadaId);
        visita.setFechaHoraProgramada(null);
        visita.setFechaHoraIngreso(null);
        visita.setEstado(EstadoVisita.PENDIENTE_APROBACION_OLVIDO);
        visita.setMotivo("Carnet olvidado");
        return visita;
    }
}