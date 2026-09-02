package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarPerfilesUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/** Recurso REST de perfiles (materias habilitadas por profesor). */
@Path("profesores/{idProfesor}/perfiles")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Perfiles")
@SecurityRequirement(name = "basicAuth")
public class PerfilResource extends RecursoBase {

    @Inject
    GestionarPerfilesUseCase useCase;

    @GET
    @Operation(summary = "Materias que el profesor esta habilitado a dictar")
    public List<MateriaVista> habilitadas(@PathParam("idProfesor") Long idProfesor) {
        return useCase.materiasHabilitadas(idProfesor);
    }

    @GET
    @Path("disponibles")
    @Operation(summary = "Materias que aun se pueden habilitar para el profesor")
    public List<MateriaVista> disponibles(@PathParam("idProfesor") Long idProfesor) {
        return useCase.materiasDisponibles(idProfesor);
    }

    @POST
    @Path("{idMateria}")
    @Operation(summary = "Habilitar una materia para el profesor")
    public Response habilitar(@PathParam("idProfesor") Long idProfesor, @PathParam("idMateria") Long idMateria) {
        useCase.habilitar(idProfesor, idMateria, contexto());
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("{idMateria}")
    @Operation(summary = "Deshabilitar una materia para el profesor")
    public Response deshabilitar(@PathParam("idProfesor") Long idProfesor, @PathParam("idMateria") Long idMateria) {
        useCase.deshabilitar(idProfesor, idMateria, contexto());
        return Response.noContent().build();
    }
}
