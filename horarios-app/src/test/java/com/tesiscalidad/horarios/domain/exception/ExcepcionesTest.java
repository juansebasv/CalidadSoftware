package com.tesiscalidad.horarios.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExcepcionesTest {

    @Test
    void codigoErrorExponeDatos() {
        assertThat(CodigoError.VALIDACION.getCodigo()).isEqualTo("ERR-VAL-001");
        assertThat(CodigoError.CONFLICTO_HORARIO.getMensajePorDefecto()).isNotBlank();
    }

    @Test
    void mensajeVacioUsaElPorDefecto() {
        ValidacionException v = new ValidacionException("   ");
        assertThat(v.getMessage()).isEqualTo(CodigoError.VALIDACION.getMensajePorDefecto());
        assertThat(v.getCodigoError()).isEqualTo(CodigoError.VALIDACION);
    }

    @Test
    void recursoNoEncontradoYConflicto() {
        RecursoNoEncontradoException r = new RecursoNoEncontradoException("Materia", 7);
        assertThat(r.getMessage()).contains("Materia").contains("7");
        assertThat(r.getCodigoError()).isEqualTo(CodigoError.RECURSO_NO_ENCONTRADO);
        assertThat(new ConflictoDatosException("dup").getCodigoError()).isEqualTo(CodigoError.CONFLICTO_DUPLICADO);
    }

    @Test
    void reglaNegocioConCodigoPersonalizado() {
        ReglaNegocioException e = new ReglaNegocioException(CodigoError.PROFESOR_SIN_PERFIL, "x");
        assertThat(e.getCodigoError()).isEqualTo(CodigoError.PROFESOR_SIN_PERFIL);
        assertThat(new ReglaNegocioException("y").getCodigoError()).isEqualTo(CodigoError.REGLA_NEGOCIO);
    }

    @Test
    void autenticacionFactorias() {
        assertThat(AutenticacionException.credencialesInvalidas().getCodigoError())
                .isEqualTo(CodigoError.CREDENCIALES_INVALIDAS);
        assertThat(AutenticacionException.cuentaBloqueada(5).getMessage()).contains("5");
        assertThat(AutenticacionException.cuentaInactiva().getCodigoError())
                .isEqualTo(CodigoError.CUENTA_INACTIVA);
    }

    @Test
    void autorizacion() {
        assertThat(new AutorizacionException("no").getCodigoError()).isEqualTo(CodigoError.NO_AUTORIZADO);
    }
}
