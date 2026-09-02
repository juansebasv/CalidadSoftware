package com.tesiscalidad.horarios.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/** Productor CDI de {@link Logger} con el nombre de la clase que lo inyecta. */
@Dependent
public class ProductorLogger {

    @Produces
    public Logger logger(InjectionPoint puntoInyeccion) {
        return LoggerFactory.getLogger(puntoInyeccion.getMember().getDeclaringClass());
    }
}
