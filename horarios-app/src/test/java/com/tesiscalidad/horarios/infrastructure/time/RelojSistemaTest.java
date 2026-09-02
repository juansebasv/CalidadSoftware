package com.tesiscalidad.horarios.infrastructure.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RelojSistemaTest {

    @Test
    void entregaLaHoraActualDelSistema() {
        Instant antes = Instant.now().minusSeconds(2);
        Instant ahora = new RelojSistema().ahora();
        Instant despues = Instant.now().plusSeconds(2);
        assertThat(ahora).isBetween(antes, despues);
        assertThat(Duration.between(ahora, Instant.now()).abs()).isLessThan(Duration.ofSeconds(5));
    }
}
