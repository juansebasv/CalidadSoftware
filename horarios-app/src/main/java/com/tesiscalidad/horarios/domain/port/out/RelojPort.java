package com.tesiscalidad.horarios.domain.port.out;

import java.time.Instant;

/** Abstraccion del reloj del sistema; permite tiempo determinista en pruebas. */
public interface RelojPort {

    Instant ahora();
}
