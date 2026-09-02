package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Vinculacion contractual del docente. {@link #codigo} = {@code profesor.tipo_contrato}. */
public enum TipoContrato {

    PLANTA("PLANTA", "Planta"),
    CATEDRA("CATEDRA", "Catedra"),
    OCASIONAL("OCASIONAL", "Ocasional");

    private final String codigo;
    private final String etiqueta;

    TipoContrato(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoContrato desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(t -> t.codigo.equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Tipo de contrato no valido: " + codigo));
    }
}
