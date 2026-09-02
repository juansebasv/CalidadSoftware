package com.tesiscalidad.horarios.adapter.in.rest.security;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;

import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Filtro de autenticacion HTTP Basic para toda la API REST salvo la coleccion
 * publica de OpenAPI y el endpoint de verificacion de credenciales. Inyecta un
 * {@link ContextoPeticion} en las propiedades de la peticion y el correlationId
 * en el MDC de logging.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AutenticacionBasicaFilter implements ContainerRequestFilter {

    public static final String PROP_CONTEXTO = "horarios.contexto";
    private static final String PREFIJO_BASIC = "Basic ";
    private static final String CID_HEADER = "X-Correlation-Id";

    private final VerificadorCredencialesApi verificador;

    @Inject
    public AutenticacionBasicaFilter(VerificadorCredencialesApi verificador) {
        this.verificador = verificador;
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String correlationId = Optional.ofNullable(ctx.getHeaderString(CID_HEADER))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", correlationId);

        String ruta = ctx.getUriInfo().getPath();
        if (ruta.startsWith("auth/login")) {
            return;
        }

        String cabecera = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BASIC)) {
            abortar(ctx, "Se requiere autenticacion HTTP Basic.");
            return;
        }
        String[] credenciales = decodificar(cabecera.substring(PREFIJO_BASIC.length()));
        if (credenciales == null) {
            abortar(ctx, "Cabecera de autenticacion mal formada.");
            return;
        }
        Optional<ContextoPeticion> contexto = verificador.autenticar(
                credenciales[0], credenciales[1].toCharArray(), ipDe(ctx), correlationId);
        if (contexto.isEmpty()) {
            abortar(ctx, "Credenciales invalidas o cuenta no habilitada.");
            return;
        }
        MDC.put("usuario", contexto.get().getUsuarioLogin());
        ctx.setProperty(PROP_CONTEXTO, contexto.get());
    }

    private String[] decodificar(String base64) {
        try {
            String plano = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int sep = plano.indexOf(':');
            if (sep < 0) {
                return null;
            }
            return new String[]{plano.substring(0, sep), plano.substring(sep + 1)};
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String ipDe(ContainerRequestContext ctx) {
        String reenviado = ctx.getHeaderString("X-Forwarded-For");
        return reenviado == null || reenviado.isBlank() ? "desconocida" : reenviado.split(",")[0].trim();
    }

    private void abortar(ContainerRequestContext ctx, String mensaje) {
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"horarios\"")
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"codigo\":\"ERR-SEC-001\",\"mensaje\":\"" + mensaje + "\"}")
                .build());
    }
}
