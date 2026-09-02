package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.support.Preconditions;

/** Parametros de la politica de bloqueo de cuenta por intentos fallidos. */
public final class PoliticaBloqueo {

    private final int maximoIntentos;
    private final int minutosBloqueo;

    private PoliticaBloqueo(int maximoIntentos, int minutosBloqueo) {
        this.maximoIntentos = maximoIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    public static PoliticaBloqueo de(int maximoIntentos, int minutosBloqueo) {
        Preconditions.rango(maximoIntentos, "maximoIntentos", 1, 20);
        Preconditions.rango(minutosBloqueo, "minutosBloqueo", 1, 1440);
        return new PoliticaBloqueo(maximoIntentos, minutosBloqueo);
    }

    public int maximoIntentos() {
        return maximoIntentos;
    }

    public int minutosBloqueo() {
        return minutosBloqueo;
    }
}
