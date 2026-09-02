package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.CrearProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
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
import java.util.List;

/** Recurso REST de la planta docente. */
@Path("profesores")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Profesores")
@SecurityRequirement(name = "basicAuth")
public class ProfesorResource extends RecursoBase {

    @Inject
    GestionarProfesoresUseCase useCase;

    @GET
    @Operation(summary = "Listar profesores (paginado, filtro de texto)")
    public Pagina<ProfesorVista> listar(@QueryParam("texto") String texto,
                                        @QueryParam("pagina") @DefaultValue("0") int pagina,
                                        @QueryParam("tamano") @DefaultValue("25") int tamano) {
        return useCase.listar(texto, pagina, tamano);
    }

    @GET
    @Path("activos")
    @Operation(summary = "Listar solo profesores activos")
    public List<ProfesorVista> activos() {
        return useCase.listarActivos();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Obtener un profesor por id")
    public ProfesorVista obtener(@PathParam("id") Long id) {
        return useCase.obtener(id);
    }

    @POST
    @Operation(summary = "Crear un profesor")
    public Response crear(ProfesorRequest cuerpo) {
        ProfesorVista creado = useCase.crear(new CrearProfesorComando(cuerpo.codigo, cuerpo.nombre,
                cuerpo.tipoContrato, cuerpo.disponible, cuerpo.idUsuario), contexto());
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(creado.getId())).build())
                .entity(creado).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Actualizar un profesor")
    public ProfesorVista actualizar(@PathParam("id") Long id, ProfesorRequest cuerpo) {
        return useCase.actualizar(id, new ActualizarProfesorComando(cuerpo.nombre, cuerpo.tipoContrato,
                cuerpo.disponible, cuerpo.idUsuario), contexto());
    }

    @PUT
    @Path("{id}/estado")
    @Operation(summary = "Activar o desactivar un profesor")
    public Response cambiarEstado(@PathParam("id") Long id, @QueryParam("activo") @DefaultValue("true") boolean activo) {
        useCase.cambiarEstado(id, activo, contexto());
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Eliminar un profesor")
    public Response eliminar(@PathParam("id") Long id) {
        useCase.eliminar(id, contexto());
        return Response.noContent().build();
    }

    /** Cuerpo de creacion/actualizacion de profesor. */
    public static class ProfesorRequest {
        public String codigo;
        public String nombre;
        public String tipoContrato;
        public boolean disponible = true;
        public Long idUsuario;
    }
}
