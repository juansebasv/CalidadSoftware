package com.tesiscalidad.horarios.infrastructure.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Adaptador de hashing con BCrypt (variante {@code $2a}, verificacion en tiempo
 * constante que realiza la libreria at.favre.lib:bcrypt). El costo es
 * configurable; si un hash almacenado usa un costo menor, {@link #requiereRehash}
 * lo indica y la capa de aplicacion lo regenera de forma transparente al
 * siguiente inicio de sesion correcto.
 */
@ApplicationScoped
public class BCryptHasheador implements HasheadorContrasenaPort {

    private static final int COSTO_POR_DEFECTO = 12;

    private int costo = COSTO_POR_DEFECTO;

    /** Requerido por CDI para el proxy de ambito. */
    protected BCryptHasheador() {
    }

    @Inject
    public BCryptHasheador(ParametrosAplicacionPort parametros) {
        this.costo = parametros.costoBcrypt();
    }

    @Override
    public ClaveHash hashear(char[] contrasenaPlano) {
        char[] copia = contrasenaPlano.clone();
        try {
            return ClaveHash.deHashExistente(BCrypt.withDefaults().hashToString(costo, copia));
        } finally {
            java.util.Arrays.fill(copia, '\0');
        }
    }

    @Override
    public boolean verificar(char[] contrasenaPlano, ClaveHash hash) {
        char[] copia = contrasenaPlano.clone();
        try {
            return BCrypt.verifyer().verify(copia, hash.valor().toCharArray()).verified;
        } finally {
            java.util.Arrays.fill(copia, '\0');
        }
    }

    @Override
    public boolean requiereRehash(ClaveHash hash) {
        return hash.costo() < costo;
    }
}
