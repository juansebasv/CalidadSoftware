package com.tesiscalidad.horarios.application.dto.auth;

import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.model.Usuario;

import java.time.Instant;

/** Identidad resultante de un login correcto. Se guarda en la sesion HTTP. */
public final class SesionAutenticada {

    private final Long idUsuario;
    private final String login;
    private final String nombre;
    private final RolUsuario rol;
    private final Instant inicioSesion;

    public SesionAutenticada(Long idUsuario, String login, String nombre, RolUsuario rol, Instant inicioSesion) {
        this.idUsuario = idUsuario;
        this.login = login;
        this.nombre = nombre;
        this.rol = rol;
        this.inicioSesion = inicioSesion;
    }

    public static SesionAutenticada de(Usuario usuario, Instant ahora) {
        return new SesionAutenticada(usuario.getId(), usuario.getLogin(), usuario.getNombre(),
                usuario.getRol(), ahora);
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getLogin() {
        return login;
    }

    public String getNombre() {
        return nombre;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Instant getInicioSesion() {
        return inicioSesion;
    }
}
