package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.support.Preconditions;

import java.util.Arrays;

/**
 * Par login + contrasena en claro entregado por el cliente durante el login.
 * La contrasena se guarda como {@code char[]} para poder borrarla de memoria
 * en cuanto se termina de verificar ({@link #limpiar()}).
 */
public final class Credenciales {

    public static final String PATRON_LOGIN = "^[A-Za-z0-9._-]{4,50}$";
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final String login;
    private final char[] contrasena;

    private Credenciales(String login, char[] contrasena) {
        this.login = login;
        this.contrasena = contrasena;
    }

    public static Credenciales de(String login, char[] contrasena) {
        String loginNormalizado = Preconditions
                .coincidePatron(login == null ? null : login.trim().toLowerCase(), "login", PATRON_LOGIN);
        Preconditions.requerido(contrasena, "contrasena");
        Preconditions.cumple(contrasena.length >= LONGITUD_MINIMA_CONTRASENA,
                "La contrasena debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        return new Credenciales(loginNormalizado, Arrays.copyOf(contrasena, contrasena.length));
    }

    public String login() {
        return login;
    }

    /** Copia defensiva; el llamador es responsable de limpiarla. */
    public char[] contrasena() {
        return Arrays.copyOf(contrasena, contrasena.length);
    }

    /** Sobrescribe la contrasena en memoria. Idempotente. */
    public void limpiar() {
        Arrays.fill(contrasena, '\0');
    }

    @Override
    public String toString() {
        return "Credenciales{login='" + login + "', contrasena=***}";
    }
}
