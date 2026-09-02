package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Dia de la semana. {@link #codigo} = columnas {@code dia} ('1'..'7', Lunes=1). */
public enum DiaSemana {

    LUNES("1", "Lunes"),
    MARTES("2", "Martes"),
    MIERCOLES("3", "Miercoles"),
    JUEVES("4", "Jueves"),
    VIERNES("5", "Viernes"),
    SABADO("6", "Sabado"),
    DOMINGO("7", "Domingo");

    private final String codigo;
    private final String etiqueta;

    DiaSemana(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getOrden() {
        return Integer.parseInt(codigo);
    }

    public static DiaSemana desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(d -> d.codigo.equals(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Dia no valido: " + codigo));
    }
}
