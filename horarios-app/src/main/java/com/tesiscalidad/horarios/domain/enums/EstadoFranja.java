package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Estado de una franja de disponibilidad. {@code horario_profesor.estado}. */
public enum EstadoFranja {

    LIBRE("0", "Libre"),
    OCUPADA("1", "Ocupada");

    private final String codigo;
    private final String etiqueta;

    EstadoFranja(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean estaLibre() {
        return this == LIBRE;
    }

    public static EstadoFranja desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(e -> e.codigo.equals(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Estado de franja no valido: " + codigo));
    }
}
