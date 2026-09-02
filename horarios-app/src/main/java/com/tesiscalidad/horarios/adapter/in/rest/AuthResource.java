package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.application.port.in.AutenticacionUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/** Recurso REST de autenticacion. */
@Path("auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticacion")
public class AuthResource extends RecursoBase {

    @Inject
    AutenticacionUseCase useCase;

    @POST
    @Path("login")
    @Operation(summary = "Verificar credenciales (aplica bloqueo por intentos y auditoria)")
    public Map<String, Object> login(LoginRequest cuerpo) {
        ContextoPeticion ctx = ContextoPeticion.anonimo(
                peticion.getHeaderString("X-Forwarded-For"),
                peticion.getHeaderString("X-Correlation-Id"));
        SesionAutenticada sesion = useCase.iniciarSesion(cuerpo.login,
                cuerpo.contrasena == null ? new char[0] : cuerpo.contrasena.toCharArray(), ctx);
        return Map.of(
                "login", sesion.getLogin(),
                "nombre", sesion.getNombre(),
                "rol", sesion.getRol().getCodigo());
    }

    @GET
    @Path("me")
    @Operation(summary = "Identidad del actor autenticado (HTTP Basic)")
    @SecurityRequirement(name = "basicAuth")
    public Map<String, String> me() {
        ContextoPeticion ctx = contexto();
        return Map.of(
                "login", ctx.getUsuarioLogin() == null ? "" : ctx.getUsuarioLogin(),
                "rol", ctx.getRol() == null ? "" : ctx.getRol().getCodigo());
    }

    /** Cuerpo de login. */
    public static class LoginRequest {
        public String login;
        public String contrasena;
    }
}
