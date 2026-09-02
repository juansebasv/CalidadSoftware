package com.tesiscalidad.horarios.infrastructure.config;

import com.tesiscalidad.horarios.domain.service.DetectorConflictosHorario;
import com.tesiscalidad.horarios.domain.service.ValidadorProgramacionSesion;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Expone los servicios de dominio puros como beans CDI sin ensuciar el paquete
 * {@code domain} con anotaciones de infraestructura.
 */
@ApplicationScoped
public class ProductorServiciosDominio {

    @Produces
    @ApplicationScoped
    public DetectorConflictosHorario detectorConflictosHorario() {
        return new DetectorConflictosHorario();
    }

    @Produces
    @ApplicationScoped
    public ValidadorProgramacionSesion validadorProgramacionSesion() {
        return new ValidadorProgramacionSesion();
    }
}
