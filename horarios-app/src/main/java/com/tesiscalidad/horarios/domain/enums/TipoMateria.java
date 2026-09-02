package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Naturaleza de una materia. {@link #codigo} = columna {@code materia.tipo}. */
public enum TipoMateria {

    TEORICA("T", "Teorica"),
    PRACTICA("P", "Practica"),
    TEORICO_PRACTICA("TP", "Teorico-practica");

    private final String codigo;
    private final String etiqueta;

    TipoMateria(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoMateria desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(t -> t.codigo.equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Tipo de materia no valido: " + codigo));
    }
}
