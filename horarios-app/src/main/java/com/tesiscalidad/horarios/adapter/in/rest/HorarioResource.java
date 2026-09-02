package com.tesiscalidad.horarios.adapter.in.rest;

import com.tesiscalidad.horarios.application.dto.horario.ConflictoVista;
import com.tesiscalidad.horarios.application.dto.horario.TableroVista;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarHorarioUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/** Recurso REST de consultas de horario y tablero. */
@Path("horario")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Horario")
@SecurityRequirement(name = "basicAuth")
public class HorarioResource extends RecursoBase {

    @Inject
    ConsultarHorarioUseCase useCase;

    @GET
    @Path("profesor/{idProfesor}")
    @Operation(summary = "Horario semanal de un profesor")
    public List<SesionClaseVista> deProfesor(@PathParam("idProfesor") Long idProfesor) {
        return useCase.horarioDeProfesor(idProfesor);
    }

    @GET
    @Path("semestre/{semestre}")
    @Operation(summary = "Horario de un semestre (opcionalmente por jornada D/N)")
    public List<SesionClaseVista> porSemestre(@PathParam("semestre") int semestre,
                                              @QueryParam("jornada") String jornada) {
        return useCase.horarioPorSemestre(semestre, jornada);
    }

    @GET
    @Path("conflictos")
    @Operation(summary = "Detectar cruces de horario en todo el plan")
    public List<ConflictoVista> conflictos() {
        return useCase.detectarConflictos();
    }

    @GET
    @Path("tablero")
    @Operation(summary = "Indicadores agregados para el tablero")
    public TableroVista tablero() {
        return useCase.tablero();
    }
}
