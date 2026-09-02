package com.tesiscalidad.horarios.domain.support;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class SupportTest {

    @Test
    void preconditionsRequerido() {
        assertThat(Preconditions.requerido("x", "campo")).isEqualTo("x");
        assertThatThrownBy(() -> Preconditions.requerido(null, "campo"))
                .isInstanceOf(ValidacionException.class).hasMessageContaining("campo");
    }

    @Test
    void preconditionsTextoYLongitud() {
        assertThat(Preconditions.textoRequerido("  hola ", "c")).isEqualTo("hola");
        assertThatThrownBy(() -> Preconditions.textoRequerido("  ", "c")).isInstanceOf(ValidacionException.class);
        assertThat(Preconditions.longitud("abcd", "c", 2, 5)).isEqualTo("abcd");
        assertThatThrownBy(() -> Preconditions.longitud("a", "c", 2, 5)).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Preconditions.longitud("abcdef", "c", 2, 5)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void preconditionsRangoPatronYCumple() {
        assertThat(Preconditions.rango(3, "c", 1, 5)).isEqualTo(3);
        assertThatThrownBy(() -> Preconditions.rango(9, "c", 1, 5)).isInstanceOf(ValidacionException.class);
        assertThat(Preconditions.coincidePatron("AB12", "c", "^[A-Z0-9]+$")).isEqualTo("AB12");
        assertThatThrownBy(() -> Preconditions.coincidePatron("ab", "c", "^[A-Z]+$")).isInstanceOf(ValidacionException.class);
        assertThatCode(() -> Preconditions.cumple(true, "ok")).doesNotThrowAnyException();
        assertThatThrownBy(() -> Preconditions.cumple(false, "malo")).isInstanceOf(ValidacionException.class);
    }

    @Test
    void paginacionAcotaTamano() {
        Paginacion p = Paginacion.de(-1, 500, 200);
        assertThat(p.pagina()).isZero();
        assertThat(p.tamano()).isEqualTo(200);
        assertThat(p.offset()).isZero();
        Paginacion p2 = Paginacion.de(3, 0, 100);
        assertThat(p2.tamano()).isEqualTo(1);
        assertThat(Paginacion.de(2, 10, 50).offset()).isEqualTo(20);
    }

    @Test
    void paginaMetadatosYMapeo() {
        Pagina<Integer> pagina = new Pagina<>(List.of(1, 2, 3), 0, 3, 7);
        assertThat(pagina.getTotalPaginas()).isEqualTo(3);
        assertThat(pagina.isPrimera()).isTrue();
        assertThat(pagina.isUltima()).isFalse();
        Pagina<String> mapeada = pagina.mapear(String::valueOf);
        assertThat(mapeada.getContenido()).containsExactly("1", "2", "3");
        assertThat(mapeada.getTotalElementos()).isEqualTo(7);
        Pagina<Integer> vacia = new Pagina<>(List.of(), 0, 0, 0);
        assertThat(vacia.getTotalPaginas()).isZero();
    }
}
