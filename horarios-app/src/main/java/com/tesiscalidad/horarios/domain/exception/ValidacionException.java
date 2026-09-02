package com.tesiscalidad.horarios.domain.exception;

/** Entrada que no cumple las invariantes del dominio. */
public class ValidacionException extends DominioException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(CodigoError.VALIDACION, mensaje);
    }
}
