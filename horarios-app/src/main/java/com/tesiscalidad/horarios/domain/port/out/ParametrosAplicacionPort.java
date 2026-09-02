package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.vo.PoliticaBloqueo;

/** Puerto de configuracion: expone los parametros ajustables de la aplicacion. */
public interface ParametrosAplicacionPort {

    int costoBcrypt();

    PoliticaBloqueo politicaBloqueo();

    int sesionTimeoutMinutos();

    int longitudMinimaContrasena();

    int tamanoPaginaPorDefecto();

    int tamanoPaginaMaximo();

    boolean auditoriaHabilitada();
}
