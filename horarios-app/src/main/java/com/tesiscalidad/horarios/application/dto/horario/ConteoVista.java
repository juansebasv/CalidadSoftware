package com.tesiscalidad.horarios.application.dto.horario;

/** Par etiqueta/valor para alimentar graficas y contadores del tablero. */
public final class ConteoVista {

    private final String etiqueta;
    private final long valor;

    public ConteoVista(String etiqueta, long valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public long getValor() {
        return valor;
    }
}
