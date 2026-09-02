package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.GestionarSesionesUseCase;
import com.tesiscalidad.horarios.domain.support.Pagina;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Recurso REST de programacion de sesiones de clase. */
@Path("sesiones")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Sesiones")
@SecurityRequirement(name = "basicAuth")
public class SesionResource extends RecursoBase {

    @Inject
    GestionarSesionesUseCase useCase;

    @GET
    @Operation(summary = "Listar sesiones (paginado, filtro por semestre)")
    public Pagina<SesionClaseVista> listar(@QueryParam("semestre") Integer semestre,
                                           @QueryParam("pagina") @DefaultValue("0") int pagina,
                                           @QueryParam("tamano") @DefaultValue("25") int tamano) {
        return useCase.listar(semestre, pagina, tamano);
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Obtener una sesion por id")
    public SesionClaseVista obtener(@PathParam("id") Long id) {
        return useCase.obtener(id);
    }

    @POST
    @Operation(summary = "Programar una sesion (valida perfil, disponibilidad y cruces)")
    public Response programar(SesionRequest cuerpo) {
        SesionClaseVista creada = useCase.programar(new ProgramarSesionComando(cuerpo.idMateria, cuerpo.idProfesor,
                cuerpo.grupo, cuerpo.dia, cuerpo.hora, cuerpo.duracion, cuerpo.semestre, cuerpo.jornada), contexto());
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(creada.getId())).build())
                .entity(creada).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Reprogramar dia/hora/duracion/jornada de una sesion")
    public SesionClaseVista reprogramar(@PathParam("id") Long id, ReprogramarRequest cuerpo) {
        return useCase.reprogramar(id, new ReprogramarSesionComando(cuerpo.dia, cuerpo.hora,
                cuerpo.duracion, cuerpo.jornada), contexto());
    }

    @PUT
    @Path("{id}/docente/{idProfesor}")
    @Operation(summary = "Asignar un docente a la sesion")
    public SesionClaseVista asignarDocente(@PathParam("id") Long id, @PathParam("idProfesor") Long idProfesor) {
        return useCase.asignarDocente(id, idProfesor, contexto());
    }

    @DELETE
    @Path("{id}/docente")
    @Operation(summary = "Quitar el docente asignado")
    public SesionClaseVista quitarDocente(@PathParam("id") Long id) {
        return useCase.quitarDocente(id, contexto());
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Cancelar (eliminar) una sesion")
    public Response cancelar(@PathParam("id") Long id) {
        useCase.cancelar(id, contexto());
        return Response.noContent().build();
    }

    /** Cuerpo de programacion de sesion. */
    public static class SesionRequest {
        public Long idMateria;
        public Long idProfesor;
        public String grupo;
        public String dia;
        public int hora;
        public int duracion;
        public int semestre;
        public String jornada;
    }

    /** Cuerpo de reprogramacion de sesion. */
    public static class ReprogramarRequest {
        public String dia;
        public int hora;
        public int duracion;
        public String jornada;
    }
}
