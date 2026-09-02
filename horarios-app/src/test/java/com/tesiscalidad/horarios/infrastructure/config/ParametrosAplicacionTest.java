package com.tesiscalidad.horarios.infrastructure.config;

import com.tesiscalidad.horarios.domain.service.DetectorConflictosHorario;
import com.tesiscalidad.horarios.domain.service.ValidadorProgramacionSesion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParametrosAplicacionTest {

    @Test
    void exponeLaConfiguracionInyectada() {
        ParametrosAplicacion p = new ParametrosAplicacion();
        p.costoBcrypt = 10;
        p.maximoIntentos = 4;
        p.minutosBloqueo = 20;
        p.sesionTimeoutMinutos = 25;
        p.longitudMinimaContrasena = 9;
        p.tamanoPaginaPorDefecto = 30;
        p.tamanoPaginaMaximo = 150;
        p.auditoriaHabilitada = false;

        assertThat(p.costoBcrypt()).isEqualTo(10);
        assertThat(p.sesionTimeoutMinutos()).isEqualTo(25);
        assertThat(p.longitudMinimaContrasena()).isEqualTo(9);
        assertThat(p.tamanoPaginaPorDefecto()).isEqualTo(30);
        assertThat(p.tamanoPaginaMaximo()).isEqualTo(150);
        assertThat(p.auditoriaHabilitada()).isFalse();
        assertThat(p.politicaBloqueo().maximoIntentos()).isEqualTo(4);
        assertThat(p.politicaBloqueo().minutosBloqueo()).isEqualTo(20);
    }

    @Test
    void productorDeServiciosDeDominioEntregaInstancias() {
        ProductorServiciosDominio productor = new ProductorServiciosDominio();
        assertThat(productor.detectorConflictosHorario()).isInstanceOf(DetectorConflictosHorario.class);
        assertThat(productor.validadorProgramacionSesion()).isInstanceOf(ValidadorProgramacionSesion.class);
    }
}
