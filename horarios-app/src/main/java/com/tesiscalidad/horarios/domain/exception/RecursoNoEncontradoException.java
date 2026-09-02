package com.tesiscalidad.horarios.domain.exception;

/** No existe el recurso referenciado por id o clave natural. */
public class RecursoNoEncontradoException extends DominioException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(CodigoError.RECURSO_NO_ENCONTRADO,
                recurso + " no encontrado (" + identificador + ")");
    }
}
