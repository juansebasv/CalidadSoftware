package com.tesiscalidad.horarios.application.dto.usuario;

/** Datos de entrada para modificar nombre y/o rol de una cuenta. */
public final class ActualizarUsuarioComando {

    private final String nombre;
    private final String rol;

    public ActualizarUsuarioComando(String nombre, String rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }
}
