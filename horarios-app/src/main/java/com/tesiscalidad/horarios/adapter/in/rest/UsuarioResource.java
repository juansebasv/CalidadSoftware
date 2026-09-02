package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.UsuarioVista;
import com.tesiscalidad.horarios.application.port.in.GestionarUsuariosUseCase;
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

/** Recurso REST de administracion de cuentas (solo rol ADMIN). */
@Path("usuarios")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuarios")
@SecurityRequirement(name = "basicAuth")
public class UsuarioResource extends RecursoBase {

    @Inject
    GestionarUsuariosUseCase useCase;

    @GET
    @Operation(summary = "Listar cuentas (paginado, filtro de texto)")
    public Pagina<UsuarioVista> listar(@QueryParam("texto") String texto,
                                       @QueryParam("pagina") @DefaultValue("0") int pagina,
                                       @QueryParam("tamano") @DefaultValue("25") int tamano) {
        return useCase.listar(texto, pagina, tamano);
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Obtener una cuenta por id")
    public UsuarioVista obtener(@PathParam("id") Long id) {
        return useCase.obtener(id);
    }

    @POST
    @Operation(summary = "Crear una cuenta")
    public Response crear(CrearUsuarioRequest cuerpo) {
        UsuarioVista creado = useCase.crear(new CrearUsuarioComando(cuerpo.login, cuerpo.nombre,
                cuerpo.contrasena == null ? new char[0] : cuerpo.contrasena.toCharArray(), cuerpo.rol), contexto());
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(creado.getId())).build())
                .entity(creado).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Actualizar nombre y rol de una cuenta")
    public UsuarioVista actualizar(@PathParam("id") Long id, ActualizarUsuarioRequest cuerpo) {
        return useCase.actualizar(id, new ActualizarUsuarioComando(cuerpo.nombre, cuerpo.rol), contexto());
    }

    @PUT
    @Path("{id}/contrasena")
    @Operation(summary = "Restablecer la contrasena de una cuenta")
    public Response cambiarContrasena(@PathParam("id") Long id, CambiarContrasenaRequest cuerpo) {
        useCase.cambiarContrasena(id, new CambiarContrasenaComando(
                cuerpo.nuevaContrasena == null ? new char[0] : cuerpo.nuevaContrasena.toCharArray()), contexto());
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/estado")
    @Operation(summary = "Activar o desactivar una cuenta")
    public Response cambiarEstado(@PathParam("id") Long id, @QueryParam("activo") @DefaultValue("true") boolean activo) {
        useCase.cambiarEstado(id, activo, contexto());
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/desbloqueo")
    @Operation(summary = "Desbloquear una cuenta bloqueada por intentos fallidos")
    public Response desbloquear(@PathParam("id") Long id) {
        useCase.desbloquear(id, contexto());
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Eliminar una cuenta")
    public Response eliminar(@PathParam("id") Long id) {
        useCase.eliminar(id, contexto());
        return Response.noContent().build();
    }

    /** Cuerpo de creacion de cuenta. */
    public static class CrearUsuarioRequest {
        public String login;
        public String nombre;
        public String contrasena;
        public String rol;
    }

    /** Cuerpo de actualizacion de cuenta. */
    public static class ActualizarUsuarioRequest {
        public String nombre;
        public String rol;
    }

    /** Cuerpo de cambio de contrasena. */
    public static class CambiarContrasenaRequest {
        public String nuevaContrasena;
    }
}
