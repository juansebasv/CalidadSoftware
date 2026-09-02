package com.tesiscalidad.horarios.domain.exception;

/** La operacion es sintacticamente valida pero rompe una regla de negocio. */
public class ReglaNegocioException extends DominioException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(CodigoError.REGLA_NEGOCIO, mensaje);
    }

    public ReglaNegocioException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
