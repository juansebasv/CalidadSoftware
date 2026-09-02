package com.tesiscalidad.horarios.application.dto.usuario;

/** Datos de entrada para crear una cuenta de usuario. */
public final class CrearUsuarioComando {

    private final String login;
    private final String nombre;
    private final char[] contrasena;
    private final String rol;

    public CrearUsuarioComando(String login, String nombre, char[] contrasena, String rol) {
        this.login = login;
        this.nombre = nombre;
        this.contrasena = contrasena == null ? new char[0] : contrasena.clone();
        this.rol = rol;
    }

    public String getLogin() {
        return login;
    }

    public String getNombre() {
        return nombre;
    }

    public char[] getContrasena() {
        return contrasena.clone();
    }

    public String getRol() {
        return rol;
    }

    /** Borra la contrasena en claro de este comando. */
    public void limpiar() {
        java.util.Arrays.fill(contrasena, '\0');
    }
}
