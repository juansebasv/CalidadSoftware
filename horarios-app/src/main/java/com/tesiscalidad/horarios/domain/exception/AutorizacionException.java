package com.tesiscalidad.horarios.domain.exception;

/** El usuario autenticado no tiene el rol necesario para la operacion. */
public class AutorizacionException extends DominioException {

    private static final long serialVersionUID = 1L;

    public AutorizacionException(String mensaje) {
        super(CodigoError.NO_AUTORIZADO, mensaje);
    }
}
