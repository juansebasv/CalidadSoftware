package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.support.Preconditions;

import java.time.Instant;

/** Evento de seguridad o de cambio de datos, para la tabla {@code auditoria}. */
public final class RegistroAuditoria {

    private final Long id;
    private final Instant fecha;
    private final String usuarioLogin;
    private final AccionAuditoria accion;
    private final String entidad;
    private final String entidadId;
    private final ResultadoAuditoria resultado;
    private final String detalle;
    private final String ip;
    private final String correlationId;

    private RegistroAuditoria(Long id, Instant fecha, String usuarioLogin, AccionAuditoria accion,
                              String entidad, String entidadId, ResultadoAuditoria resultado,
                              String detalle, String ip, String correlationId) {
        this.id = id;
        this.fecha = fecha;
        this.usuarioLogin = usuarioLogin;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.detalle = detalle;
        this.ip = ip;
        this.correlationId = correlationId;
    }

    public static Builder builder(AccionAuditoria accion, ResultadoAuditoria resultado) {
        return new Builder(Preconditions.requerido(accion, "accion"),
                Preconditions.requerido(resultado, "resultado"));
    }

    public static RegistroAuditoria reconstruir(Long id, Instant fecha, String usuarioLogin, AccionAuditoria accion,
                                                String entidad, String entidadId, ResultadoAuditoria resultado,
                                                String detalle, String ip, String correlationId) {
        return new RegistroAuditoria(id, fecha, usuarioLogin, accion, entidad, entidadId,
                resultado, detalle, ip, correlationId);
    }

    public Long getId() {
        return id;
    }

    public Instant getFecha() {
        return fecha;
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public ResultadoAuditoria getResultado() {
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

    /** Builder fluido: solo accion y resultado son obligatorios. */
    public static final class Builder {

        private final AccionAuditoria accion;
        private final ResultadoAuditoria resultado;
        private Instant fecha = Instant.now();
        private String usuarioLogin;
        private String entidad;
        private String entidadId;
        private String detalle;
        private String ip;
        private String correlationId;

        private Builder(AccionAuditoria accion, ResultadoAuditoria resultado) {
            this.accion = accion;
            this.resultado = resultado;
        }

        public Builder fecha(Instant fecha) {
            this.fecha = fecha;
            return this;
        }

        public Builder usuario(String login) {
            this.usuarioLogin = login;
            return this;
        }

        public Builder entidad(String entidad, Object id) {
            this.entidad = entidad;
            this.entidadId = id == null ? null : String.valueOf(id);
            return this;
        }

        public Builder detalle(String detalle) {
            this.detalle = detalle;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public RegistroAuditoria build() {
            return new RegistroAuditoria(null, fecha, usuarioLogin, accion, entidad, entidadId,
                    resultado, detalle, ip, correlationId);
        }
    }
}
