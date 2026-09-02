package com.tesiscalidad.horarios.domain.exception;

import java.util.Objects;

/**
 * Raiz de todas las excepciones de negocio. Cada instancia lleva un
 * {@link CodigoError} estable; el mensaje es apto para mostrar al usuario.
 */
public abstract class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient CodigoError codigoError;

    protected DominioException(CodigoError codigoError, String mensaje) {
        super(mensaje == null || mensaje.isBlank() ? codigoError.getMensajePorDefecto() : mensaje);
        this.codigoError = Objects.requireNonNull(codigoError, "codigoError es obligatorio");
    }

    protected DominioException(CodigoError codigoError, String mensaje, Throwable causa) {
        super(mensaje == null || mensaje.isBlank() ? codigoError.getMensajePorDefecto() : mensaje, causa);
        this.codigoError = Objects.requireNonNull(codigoError, "codigoError es obligatorio");
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }
}
