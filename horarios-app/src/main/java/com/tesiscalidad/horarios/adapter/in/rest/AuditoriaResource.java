package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.auditoria.AuditoriaVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarAuditoriaUseCase;
import com.tesiscalidad.horarios.domain.support.Pagina;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/** Recurso REST de consulta de la traza de auditoria. */
@Path("auditoria")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Auditoria")
@SecurityRequirement(name = "basicAuth")
public class AuditoriaResource extends RecursoBase {

    @Inject
    ConsultarAuditoriaUseCase useCase;

    @GET
    @Operation(summary = "Listar la traza de auditoria (paginado, filtros por usuario y accion)")
    public Pagina<AuditoriaVista> listar(@QueryParam("usuario") String usuario,
                                         @QueryParam("accion") String accion,
                                         @QueryParam("pagina") @DefaultValue("0") int pagina,
                                         @QueryParam("tamano") @DefaultValue("25") int tamano) {
        return useCase.listar(usuario, accion, pagina, tamano, contexto());
    }
}
