package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.materia.ActualizarMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarMateriasUseCase;
import com.tesiscalidad.horarios.domain.support.Pagina;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
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

/** Recurso REST del catalogo de materias. */
@Path("materias")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Materias")
@SecurityRequirement(name = "basicAuth")
public class MateriaResource extends RecursoBase {

    @Inject
    GestionarMateriasUseCase useCase;

    @GET
    @Operation(summary = "Listar materias (paginado y con filtro de texto/semestre)")
    @APIResponse(responseCode = "200", description = "Pagina de materias")
    public Pagina<MateriaVista> listar(@QueryParam("texto") String texto,
                                       @QueryParam("semestre") Integer semestre,
                                       @QueryParam("pagina") @DefaultValue("0") int pagina,
                                       @QueryParam("tamano") @DefaultValue("25") int tamano) {
        return useCase.listar(texto, semestre, pagina, tamano);
    }

    @GET
    @Path("activas")
    @Operation(summary = "Listar solo materias activas")
    public java.util.List<MateriaVista> activas() {
        return useCase.listarActivas();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Obtener una materia por id")
    @APIResponse(responseCode = "404", description = "No existe")
    public MateriaVista obtener(@PathParam("id") Long id) {
        return useCase.obtener(id);
    }

    @POST
    @Operation(summary = "Crear una materia")
    @APIResponse(responseCode = "201", description = "Creada")
    @APIResponse(responseCode = "409", description = "Codigo duplicado")
    public Response crear(MateriaRequest cuerpo) {
        MateriaVista creada = useCase.crear(new CrearMateriaComando(cuerpo.codigo, cuerpo.nombre, cuerpo.tipo,
                cuerpo.creditos, cuerpo.intensidadHoraria, cuerpo.semestre), contexto());
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(creada.getId())).build())
                .entity(creada).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Actualizar una materia")
    public MateriaVista actualizar(@PathParam("id") Long id, MateriaRequest cuerpo) {
        return useCase.actualizar(id, new ActualizarMateriaComando(cuerpo.nombre, cuerpo.tipo,
                cuerpo.creditos, cuerpo.intensidadHoraria, cuerpo.semestre), contexto());
    }

    @PUT
    @Path("{id}/estado")
    @Operation(summary = "Activar o desactivar una materia")
    public Response cambiarEstado(@PathParam("id") Long id, @QueryParam("activa") @DefaultValue("true") boolean activa) {
        useCase.cambiarEstado(id, activa, contexto());
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Eliminar una materia")
    public Response eliminar(@PathParam("id") Long id) {
        useCase.eliminar(id, contexto());
        return Response.noContent().build();
    }

    /** Cuerpo de creacion/actualizacion de materia. */
    public static class MateriaRequest {
        public String codigo;
        public String nombre;
        public String tipo;
        public int creditos;
        public int intensidadHoraria;
        public int semestre;
    }
}
