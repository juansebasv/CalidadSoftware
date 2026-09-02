package com.tesiscalidad.horarios.adapter.in.rest.mapper;

import com.tesiscalidad.horarios.adapter.in.rest.dto.RespuestaError;
import com.tesiscalidad.horarios.domain.exception.CodigoError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** Ultima linea de defensa: cualquier excepcion no controlada -> 500 con cuerpo JSON. */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            Response original = ((WebApplicationException) ex).getResponse();
            if (original != null && original.getStatus() < 500) {
                return original;
            }
        }
        String ruta = uriInfo == null ? null : "/" + uriInfo.getPath();
        LOG.error("Error no controlado en la API", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new RespuestaError(CodigoError.INTERNO.getCodigo(),
                        CodigoError.INTERNO.getMensajePorDefecto(), ruta))
                .build();
    }
}
