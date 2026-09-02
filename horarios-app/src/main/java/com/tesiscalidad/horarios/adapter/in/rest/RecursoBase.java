package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.adapter.in.rest.security.AutenticacionBasicaFilter;
import com.tesiscalidad.horarios.application.dto.ContextoPeticion;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.container.ContainerRequestContext;

/** Utilidades compartidas por los recursos REST. */
public abstract class RecursoBase {

    @Context
    protected ContainerRequestContext peticion;

    @Context
    protected UriInfo uriInfo;

    /** Contexto del actor autenticado, poblado por {@link AutenticacionBasicaFilter}. */
    protected ContextoPeticion contexto() {
        Object valor = peticion.getProperty(AutenticacionBasicaFilter.PROP_CONTEXTO);
        if (valor instanceof ContextoPeticion) {
            return (ContextoPeticion) valor;
        }
        return ContextoPeticion.anonimo("desconocida", null);
    }
}
