package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.support.Preconditions;

import java.util.Objects;

/**
 * Codigo natural en mayusculas (materias, profesores). Inmutable y auto-validado.
 */
public final class Codigo {

    public static final String PATRON = "^[A-Z0-9-]{3,20}$";

    private final String valor;

    private Codigo(String valor) {
        this.valor = valor;
    }

    public static Codigo de(String bruto) {
        String normalizado = Preconditions.textoRequerido(bruto, "codigo").toUpperCase();
        return new Codigo(Preconditions.coincidePatron(normalizado, "codigo", PATRON));
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Codigo)) {
            return false;
        }
        return valor.equals(((Codigo) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
