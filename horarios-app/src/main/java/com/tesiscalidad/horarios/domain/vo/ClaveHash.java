package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.support.Preconditions;

import java.util.Objects;

/**
 * Hash BCrypt de una contrasena (formato {@code $2a/$2b/$2y$<cost>$<53 chars>}).
 * Nunca contiene la contrasena en claro.
 */
public final class ClaveHash {

    public static final String PATRON = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    private final String valor;

    private ClaveHash(String valor) {
        this.valor = valor;
    }

    public static ClaveHash deHashExistente(String hash) {
        return new ClaveHash(Preconditions.coincidePatron(hash, "clave", PATRON));
    }

    public String valor() {
        return valor;
    }

    public int costo() {
        return Integer.parseInt(valor.substring(4, 6));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClaveHash)) {
            return false;
        }
        return valor.equals(((ClaveHash) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return "ClaveHash{***}";
    }
}
