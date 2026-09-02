package com.tesiscalidad.horarios.application.support;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;

import javax.enterprise.context.ApplicationScoped;

/**
 * Comprobaciones de autorizacion basadas en rol. Se invoca al inicio de cada
 * caso de uso que muta datos (defensa en profundidad respecto al filtro web).
 */
@ApplicationScoped
public class Autorizador {

    public void exigeGestion(ContextoPeticion contexto) {
        RolUsuario rol = rolDe(contexto);
        if (!rol.puedeGestionar()) {
            throw new AutorizacionException(
                    "El rol " + rol.getCodigo() + " no puede realizar operaciones de gestion.");
        }
    }

    public void exigeAdministracionUsuarios(ContextoPeticion contexto) {
        RolUsuario rol = rolDe(contexto);
        if (!rol.puedeAdministrarUsuarios()) {
            throw new AutorizacionException(
                    "El rol " + rol.getCodigo() + " no puede administrar cuentas de usuario.");
        }
    }

    private RolUsuario rolDe(ContextoPeticion contexto) {
        if (contexto == null || contexto.esAnonimo()) {
            throw new AutorizacionException("Operacion no permitida sin sesion.");
        }
        return contexto.getRol();
    }
}
