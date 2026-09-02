package com.tesiscalidad.horarios.support;

import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.vo.PoliticaBloqueo;

/** Implementacion de parametros con valores fijos para pruebas. */
public final class ParametrosFalsos implements ParametrosAplicacionPort {

    private boolean auditoria = true;
    private int costoBcrypt = 12;

    public ParametrosFalsos conAuditoria(boolean valor) {
        this.auditoria = valor;
        return this;
    }

    public ParametrosFalsos conCostoBcrypt(int valor) {
        this.costoBcrypt = valor;
        return this;
    }

    @Override
    public int costoBcrypt() {
        return costoBcrypt;
    }

    @Override
    public PoliticaBloqueo politicaBloqueo() {
        return PoliticaBloqueo.de(3, 15);
    }

    @Override
    public int sesionTimeoutMinutos() {
        return 30;
    }

    @Override
    public int longitudMinimaContrasena() {
        return 8;
    }

    @Override
    public int tamanoPaginaPorDefecto() {
        return 25;
    }

    @Override
    public int tamanoPaginaMaximo() {
        return 200;
    }

    @Override
    public boolean auditoriaHabilitada() {
        return auditoria;
    }
}
