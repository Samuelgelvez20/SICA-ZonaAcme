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

    // NO se agregan aún crearNoAnunciada ni crearPorOlvido (HU-09 y HU-11)
}