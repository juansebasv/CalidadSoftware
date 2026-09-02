package com.tesiscalidad.horarios.adapter.in.rest;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Raiz de la API REST. La documentacion OpenAPI/Swagger se genera con
 * MicroProfile OpenAPI y queda publicada en {@code /openapi} (y {@code /openapi-ui}).
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
        info = @Info(
                title = "API - Sistema de Asignacion de Horarios Docentes",
                version = "1.0.0",
                description = "Operaciones de gestion academica: materias, profesores, usuarios, "
                        + "perfiles, disponibilidad, sesiones de clase, consultas de horario y auditoria. "
                        + "Autenticacion HTTP Basic con las cuentas del sistema.",
                contact = @Contact(name = "Calidad de Software")),
        servers = @Server(url = "/", description = "Servidor actual"),
        tags = {
                @Tag(name = "Autenticacion", description = "Login y verificacion de identidad"),
                @Tag(name = "Materias", description = "Catalogo de asignaturas"),
                @Tag(name = "Profesores", description = "Planta docente"),
                @Tag(name = "Usuarios", description = "Cuentas de acceso"),
                @Tag(name = "Perfiles", description = "Habilitacion profesor-materia"),
                @Tag(name = "Disponibilidad", description = "Rejilla horaria del docente"),
                @Tag(name = "Sesiones", description = "Programacion de clases"),
                @Tag(name = "Horario", description = "Consultas y tablero"),
                @Tag(name = "Auditoria", description = "Traza de seguridad y cambios")
        })
@SecurityScheme(
        securitySchemeName = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Credenciales de una cuenta del sistema (ver datos semilla)")
public class JaxRsApplication extends Application {
}
