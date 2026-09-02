package com.tesiscalidad.horarios.support;

import com.tesiscalidad.horarios.domain.port.out.RelojPort;

import java.time.Instant;

/** Reloj determinista para pruebas. */
public final class RelojFijo implements RelojPort {

    private Instant ahora;

    public RelojFijo(Instant ahora) {
        this.ahora = ahora;
    }

    public static RelojFijo en(String iso) {
        return new RelojFijo(Instant.parse(iso));
    }

    public void avanzarMinutos(long minutos) {
        this.ahora = this.ahora.plusSeconds(minutos * 60);
    }

    @Override
    public Instant ahora() {
        return ahora;
    }
}
