package com.tesiscalidad.horarios.application.dto.usuario;

import java.util.Arrays;

/** Datos de entrada para restablecer la contrasena de una cuenta. */
public final class CambiarContrasenaComando {

    private final char[] nuevaContrasena;

    public CambiarContrasenaComando(char[] nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena == null ? new char[0] : nuevaContrasena.clone();
    }

    public char[] getNuevaContrasena() {
        return nuevaContrasena.clone();
    }

    public void limpiar() {
        Arrays.fill(nuevaContrasena, '\0');
    }
}
