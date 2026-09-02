package com.tesiscalidad.horarios.domain.support;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;

/** Guardas de invariantes del dominio. Lanzan {@link ValidacionException}. */
public final class Preconditions {

    private Preconditions() {
    }

    public static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        return valor;
    }

    public static String textoRequerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    public static String longitud(String valor, String campo, int min, int max) {
        String limpio = textoRequerido(valor, campo);
        if (limpio.length() < min || limpio.length() > max) {
            throw new ValidacionException(
                    "El campo '" + campo + "' debe tener entre " + min + " y " + max + " caracteres.");
        }
        return limpio;
    }

    public static int rango(int valor, String campo, int min, int max) {
        if (valor < min || valor > max) {
            throw new ValidacionException(
                    "El campo '" + campo + "' debe estar entre " + min + " y " + max + ".");
        }
        return valor;
    }

    public static void cumple(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ValidacionException(mensaje);
        }
    }

    public static String coincidePatron(String valor, String campo, String regex) {
        String limpio = textoRequerido(valor, campo);
        if (!limpio.matches(regex)) {
            throw new ValidacionException("El campo '" + campo + "' tiene un formato no valido.");
        }
        return limpio;
    }
}
