package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Jornada academica. {@link #codigo} = columna {@code horario_materia.jornada}. */
public enum Jornada {

    DIURNA("D", "Diurna"),
    NOCTURNA("N", "Nocturna");

    private final String codigo;
    private final String etiqueta;

    Jornada(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Jornada desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(j -> j.codigo.equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Jornada no valida: " + codigo));
    }
}
