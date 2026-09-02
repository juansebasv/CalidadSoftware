package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/** Origen de un registro de disponibilidad. {@code horario_profesor.manual}. */
public enum OrigenRegistro {

    GENERADO("0", "Generado"),
    MANUAL("1", "Manual");

    private final String codigo;
    private final String etiqueta;

    OrigenRegistro(String codigo, String etiqueta) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static OrigenRegistro desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(o -> o.codigo.equals(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Origen de registro no valido: " + codigo));
    }
}
