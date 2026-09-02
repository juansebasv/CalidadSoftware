package com.tesiscalidad.horarios.application.dto;

import com.tesiscalidad.horarios.domain.enums.RolUsuario;

/**
 * Datos del actor y de la peticion en curso. Los casos de uso lo reciben para
 * autorizar la operacion y para poblar la traza de auditoria.
 */
public final class ContextoPeticion {

    private final String usuarioLogin;
    private final RolUsuario rol;
    private final String ip;
    private final String correlationId;

    public ContextoPeticion(String usuarioLogin, RolUsuario rol, String ip, String correlationId) {
        this.usuarioLogin = usuarioLogin;
        this.rol = rol;
        this.ip = ip;
        this.correlationId = correlationId;
    }

    /** Contexto sin usuario autenticado (login, health-check, etc.). */
    public static ContextoPeticion anonimo(String ip, String correlationId) {
        return new ContextoPeticion(null, null, ip, correlationId);
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public String getIp() {
        return ip;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean esAnonimo() {
        return rol == null;
    }
}
