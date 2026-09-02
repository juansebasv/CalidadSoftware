package com.tesiscalidad.horarios.infrastructure.security;

import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptHasheadorTest {

    @Test
    void hasheaVerificaYDetectaRehash() {
        BCryptHasheador hasheador = new BCryptHasheador(new ParametrosFalsos().conCostoBcrypt(4));
        ClaveHash hash = hasheador.hashear("Secreta123".toCharArray());

        assertThat(hash.valor()).matches(ClaveHash.PATRON);
        assertThat(hash.costo()).isEqualTo(4);
        assertThat(hasheador.verificar("Secreta123".toCharArray(), hash)).isTrue();
        assertThat(hasheador.verificar("incorrecta".toCharArray(), hash)).isFalse();
        assertThat(hasheador.requiereRehash(hash)).isFalse();
        assertThat(new BCryptHasheador(new ParametrosFalsos().conCostoBcrypt(6)).requiereRehash(hash)).isTrue();
    }

    @Test
    void constructorSinArgumentosUsaCostoPorDefecto() {
        BCryptHasheador hasheador = new BCryptHasheador();
        ClaveHash hash = hasheador.hashear("ClavePorDefecto1".toCharArray());
        assertThat(hash.costo()).isEqualTo(12);
        assertThat(hasheador.verificar("ClavePorDefecto1".toCharArray(), hash)).isTrue();
    }
}
