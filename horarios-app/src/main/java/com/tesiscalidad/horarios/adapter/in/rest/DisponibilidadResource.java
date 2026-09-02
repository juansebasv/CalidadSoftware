package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.disponibilidad.FranjaDisponibilidadVista;
import com.tesiscalidad.horarios.application.port.in.GestionarDisponibilidadUseCase;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/** Recurso REST de la rejilla de disponibilidad docente. */
@Path("profesores/{idProfesor}/disponibilidad")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Disponibilidad")
@SecurityRequirement(name = "basicAuth")
public class DisponibilidadResource extends RecursoBase {

    @Inject
    GestionarDisponibilidadUseCase useCase;

    @GET
    @Operation(summary = "Listar la disponibilidad del profesor")
    public List<FranjaDisponibilidadVista> listar(@PathParam("idProfesor") Long idProfesor) {
        return useCase.listar(idProfesor);
    }

    @POST
    @Operation(summary = "Marcar una franja de disponibilidad")
    public Response marcar(@PathParam("idProfesor") Long idProfesor,
                           @QueryParam("dia") String dia, @QueryParam("hora") int hora) {
        FranjaDisponibilidadVista creada = useCase.marcar(idProfesor, dia, hora, contexto());
        return Response.status(Response.Status.CREATED).entity(creada).build();
    }

    @DELETE
    @Operation(summary = "Liberar una franja de disponibilidad")
    public Response liberar(@PathParam("idProfesor") Long idProfesor,
                            @QueryParam("dia") String dia, @QueryParam("hora") int hora) {
        useCase.liberar(idProfesor, dia, hora, contexto());
        return Response.noContent().build();
    }

    @POST
    @Path("rejilla")
    @Operation(summary = "Generar una rejilla de disponibilidad para varios dias/horas")
    public Response generar(@PathParam("idProfesor") Long idProfesor,
                            @QueryParam("dias") String dias,
                            @QueryParam("horaInicio") int horaInicio,
                            @QueryParam("horaFin") int horaFin) {
        int creadas = useCase.generarRejilla(idProfesor,
                dias == null ? List.of() : List.of(dias.split(",")), horaInicio, horaFin, contexto());
        return Response.ok("{\"creadas\":" + creadas + "}").build();
    }

    @DELETE
    @Path("todo")
    @Operation(summary = "Eliminar toda la disponibilidad del profesor")
    public Response limpiar(@PathParam("idProfesor") Long idProfesor) {
        useCase.limpiar(idProfesor, contexto());
        return Response.noContent().build();
    }
}
