package com.tesiscalidad.horarios.domain.exception;

/** Violacion de una clave natural unica (codigo o login duplicado, etc.). */
public class ConflictoDatosException extends DominioException {

    private static final long serialVersionUID = 1L;

    public ConflictoDatosException(String mensaje) {
        super(CodigoError.CONFLICTO_DUPLICADO, mensaje);
    }
}
