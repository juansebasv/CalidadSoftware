package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void codigoNormalizaYValida() {
        Codigo c = Codigo.de("inf-101");
        assertThat(c.valor()).isEqualTo("INF-101");
        assertThat(c.toString()).isEqualTo("INF-101");
        assertThat(c).isEqualTo(Codigo.de("INF-101")).hasSameHashCodeAs(Codigo.de("INF-101"));
        assertThat(c.equals(c)).isTrue();
        assertThat(c.equals("INF-101")).isFalse();
        assertThatThrownBy(() -> Codigo.de("a")).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Codigo.de("con espacio")).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Codigo.de(null)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void franjaCalculaFinYValida() {
        Franja f = Franja.de(DiaSemana.LUNES, 8, 2);
        assertThat(f.horaFin()).isEqualTo(10);
        assertThat(f.duracionBloques()).isEqualTo(2);
        assertThat(f.toString()).contains("Lunes");
        assertThatThrownBy(() -> Franja.de(DiaSemana.LUNES, 5, 1)).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Franja.de(DiaSemana.LUNES, 8, 99)).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Franja.de(null, 8, 1)).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Franja.de(DiaSemana.LUNES, 22, 5)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void franjaSolapamiento() {
        Franja a = Franja.de(DiaSemana.LUNES, 8, 2);   // 8-10
        Franja b = Franja.de(DiaSemana.LUNES, 9, 2);   // 9-11 -> solapa
        Franja c = Franja.de(DiaSemana.LUNES, 10, 1);  // 10-11 -> no solapa (limite)
        Franja d = Franja.de(DiaSemana.MARTES, 8, 2);  // otro dia
        assertThat(a.seSolapaCon(b)).isTrue();
        assertThat(a.seSolapaCon(c)).isFalse();
        assertThat(a.seSolapaCon(d)).isFalse();
        assertThat(a.mismoDia(d)).isFalse();
        assertThat(a.cubreHora(DiaSemana.LUNES, 9)).isTrue();
        assertThat(a.cubreHora(DiaSemana.LUNES, 10)).isFalse();
        assertThat(a.seSolapaCon(null)).isFalse();
        assertThat(a).isEqualTo(Franja.de(DiaSemana.LUNES, 8, 2))
                .hasSameHashCodeAs(Franja.de(DiaSemana.LUNES, 8, 2));
        assertThat(a.equals(a)).isTrue();
        assertThat(a.equals(b)).isFalse();
        assertThat(a.equals("x")).isFalse();
        assertThat(Franja.puntual(DiaSemana.LUNES, 8).duracionBloques()).isEqualTo(1);
    }

    @Test
    void claveHashValidaFormatoYCosto() {
        ClaveHash h = ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");
        assertThat(h.costo()).isEqualTo(12);
        assertThat(h.toString()).doesNotContain("$2a");
        assertThat(h).isEqualTo(ClaveHash.deHashExistente(h.valor())).hasSameHashCodeAs(ClaveHash.deHashExistente(h.valor()));
        assertThat(h.equals("x")).isFalse();
        assertThatThrownBy(() -> ClaveHash.deHashExistente("texto-plano")).isInstanceOf(ValidacionException.class);
    }

    @Test
    void credencialesNormalizaLimpiaYCopia() {
        char[] clave = "Secreta12".toCharArray();
        Credenciales c = Credenciales.de("  JDPerez ", clave);
        assertThat(c.login()).isEqualTo("jdperez");
        char[] copia = c.contrasena();
        assertThat(copia).isEqualTo("Secreta12".toCharArray());
        copia[0] = 'X';
        assertThat(c.contrasena()[0]).isEqualTo('S'); // copia defensiva
        c.limpiar();
        assertThat(c.contrasena()).containsOnly('\0');
        assertThat(c.toString()).contains("jdperez").doesNotContain("Secreta");
    }

    @Test
    void credencialesRechazaEntradasInvalidas() {
        assertThatThrownBy(() -> Credenciales.de("ab", "12345678".toCharArray()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Credenciales.de("valido", "corta".toCharArray()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Credenciales.de("valido", null)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void politicaBloqueoValida() {
        PoliticaBloqueo p = PoliticaBloqueo.de(5, 30);
        assertThat(p.maximoIntentos()).isEqualTo(5);
        assertThat(p.minutosBloqueo()).isEqualTo(30);
        assertThatThrownBy(() -> PoliticaBloqueo.de(0, 30)).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> PoliticaBloqueo.de(5, 0)).isInstanceOf(ValidacionException.class);
    }
}
