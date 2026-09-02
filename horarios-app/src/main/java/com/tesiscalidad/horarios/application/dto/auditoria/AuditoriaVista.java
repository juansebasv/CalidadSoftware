package com.tesiscalidad.horarios.application.dto.auditoria;

import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Proyeccion de lectura de un registro de auditoria. */
public final class AuditoriaVista {

    private final Long id;
    private final Instant fecha;
    private final String usuario;
    private final String accion;
    private final String entidad;
    private final String entidadId;
    private final String resultado;
    private final String detalle;
    private final String ip;
    private final String correlationId;

    @SuppressWarnings("java:S107")
    public AuditoriaVista(Long id, Instant fecha, String usuario, String accion, String entidad,
                          String entidadId, String resultado, String detalle, String ip, String correlationId) {
        this.id = id;
        this.fecha = fecha;
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.detalle = detalle;
        this.ip = ip;
        this.correlationId = correlationId;
    }

    public static AuditoriaVista de(RegistroAuditoria r) {
        return new AuditoriaVista(r.getId(), r.getFecha(), r.getUsuarioLogin(),
                r.getAccion().name(), r.getEntidad(), r.getEntidadId(),
                r.getResultado().name(), r.getDetalle(), r.getIp(), r.getCorrelationId());
    }

    public Long getId() {
        return id;
    }

    public Instant getFecha() {
        return fecha;
    }

    /** Fecha lista para mostrar en la UI (zona America/Bogota). */
    public String getFechaTexto() {
        return fecha == null ? "-" : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("America/Bogota")).format(fecha);
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public String getResultado() {
        return resultado;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getIp() {
        return ip;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
