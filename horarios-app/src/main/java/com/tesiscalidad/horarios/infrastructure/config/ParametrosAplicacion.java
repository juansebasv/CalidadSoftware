package com.tesiscalidad.horarios.infrastructure.config;

import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.vo.PoliticaBloqueo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Adaptador de configuracion sobre MicroProfile Config. Los valores provienen
 * de {@code META-INF/microprofile-config.properties} y pueden sobreescribirse
 * por variable de entorno (ver docker-compose.yml).
 */
@ApplicationScoped
public class ParametrosAplicacion implements ParametrosAplicacionPort {

    @Inject
    @ConfigProperty(name = "app.security.bcrypt-cost", defaultValue = "12")
    int costoBcrypt;

    @Inject
    @ConfigProperty(name = "app.security.max-login-attempts", defaultValue = "5")
    int maximoIntentos;

    @Inject
    @ConfigProperty(name = "app.security.lockout-minutes", defaultValue = "30")
    int minutosBloqueo;

    @Inject
    @ConfigProperty(name = "app.security.session-timeout-minutes", defaultValue = "30")
    int sesionTimeoutMinutos;

    @Inject
    @ConfigProperty(name = "app.security.password-min-length", defaultValue = "8")
    int longitudMinimaContrasena;

    @Inject
    @ConfigProperty(name = "app.pagination.default-size", defaultValue = "25")
    int tamanoPaginaPorDefecto;

    @Inject
    @ConfigProperty(name = "app.pagination.max-size", defaultValue = "200")
    int tamanoPaginaMaximo;

    @Inject
    @ConfigProperty(name = "app.audit.enabled", defaultValue = "true")
    boolean auditoriaHabilitada;

    @Override
    public int costoBcrypt() {
        return costoBcrypt;
    }

    @Override
    public PoliticaBloqueo politicaBloqueo() {
        return PoliticaBloqueo.de(maximoIntentos, minutosBloqueo);
    }

    @Override
    public int sesionTimeoutMinutos() {
        return sesionTimeoutMinutos;
    }

    @Override
    public int longitudMinimaContrasena() {
        return longitudMinimaContrasena;
    }

    @Override
    public int tamanoPaginaPorDefecto() {
        return tamanoPaginaPorDefecto;
    }

    @Override
    public int tamanoPaginaMaximo() {
        return tamanoPaginaMaximo;
    }

    @Override
    public boolean auditoriaHabilitada() {
        return auditoriaHabilitada;
    }
}
