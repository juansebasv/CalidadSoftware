package com.tesiscalidad.horarios.infrastructure.time;

import com.tesiscalidad.horarios.domain.port.out.RelojPort;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;

/** Adaptador del reloj real del sistema. */
@ApplicationScoped
public class RelojSistema implements RelojPort {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
