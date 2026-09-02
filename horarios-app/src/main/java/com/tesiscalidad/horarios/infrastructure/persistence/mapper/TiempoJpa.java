package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Conversion entre {@link Instant} (dominio) y {@link OffsetDateTime} (columnas timestamptz). */
public final class TiempoJpa {

    private TiempoJpa() {
    }

    public static Instant aInstant(OffsetDateTime valor) {
        return valor == null ? null : valor.toInstant();
    }

    public static OffsetDateTime aOffset(Instant valor) {
        return valor == null ? null : OffsetDateTime.ofInstant(valor, ZoneOffset.UTC);
    }
}
