package com.tesiscalidad.horarios.adapter.in.rest.mapper;

import com.tesiscalidad.horarios.adapter.in.rest.dto.RespuestaError;
import com.tesiscalidad.horarios.domain.exception.AutenticacionException;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.DominioException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** Traduce las excepciones de dominio a respuestas HTTP con cuerpo JSON estable. */
@Provider
public class DominioExceptionMapper implements ExceptionMapper<DominioException> {

    private static final Logger LOG = LoggerFactory.getLogger(DominioExceptionMapper.class);

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_UNPROCESSABLE = 422;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(DominioException ex) {
        int estado = estadoDe(ex);
        String ruta = uriInfo == null ? null : "/" + uriInfo.getPath();
        if (estado >= 500) {
            LOG.error("Error de dominio no clasificado [{}]", ex.getCodigoError().getCodigo(), ex);
        } else {
            LOG.warn("Peticion rechazada [{}]: {}", ex.getCodigoError().getCodigo(), ex.getMessage());
        }
        return Response.status(estado)
                .entity(new RespuestaError(ex.getCodigoError().getCodigo(), ex.getMessage(), ruta))
                .build();
    }

    private int estadoDe(DominioException ex) {
        if (ex instanceof ValidacionException) {
            return HTTP_BAD_REQUEST;
        }
        if (ex instanceof RecursoNoEncontradoException) {
            return HTTP_NOT_FOUND;
        }
        if (ex instanceof ConflictoDatosException) {
            return HTTP_CONFLICT;
        }
        if (ex instanceof AutenticacionException) {
            return HTTP_UNAUTHORIZED;
        }
        if (ex instanceof AutorizacionException) {
            return HTTP_FORBIDDEN;
        }
        return HTTP_UNPROCESSABLE;
    }
}
