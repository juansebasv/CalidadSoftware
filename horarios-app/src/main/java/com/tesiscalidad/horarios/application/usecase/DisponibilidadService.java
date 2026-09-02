package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.disponibilidad.FranjaDisponibilidadVista;
import com.tesiscalidad.horarios.application.port.in.GestionarDisponibilidadUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Implementacion del caso de uso de disponibilidad docente. */
@ApplicationScoped
public class DisponibilidadService implements GestionarDisponibilidadUseCase {

    private static final String ENTIDAD = "DISPONIBILIDAD";

    private final DisponibilidadRepositoryPort repositorio;
    private final ProfesorRepositoryPort profesorRepositorio;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    protected DisponibilidadService() {
        this(null, null, null, null);
    }

    @Inject
    public DisponibilidadService(DisponibilidadRepositoryPort repositorio,
                                 ProfesorRepositoryPort profesorRepositorio,
                                 Autorizador autorizador, TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    public List<FranjaDisponibilidadVista> listar(Long idProfesor) {
        exigeProfesor(idProfesor);
        return repositorio.listarPorProfesor(idProfesor).stream()
                .map(FranjaDisponibilidadVista::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FranjaDisponibilidadVista marcar(Long idProfesor, String dia, int hora, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        exigeProfesor(idProfesor);
        DiaSemana diaSemana = DiaSemana.desdeCodigo(dia);
        if (repositorio.buscar(idProfesor, diaSemana, hora).isPresent()) {
            throw new ConflictoDatosException("La franja ya existe para ese profesor");
        }
        DisponibilidadProfesor creada = repositorio.guardar(
                DisponibilidadProfesor.crear(idProfesor, diaSemana, hora, OrigenRegistro.MANUAL));
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, creada.getId(),
                "Franja " + diaSemana.getEtiqueta() + " " + hora + "h para profesor " + idProfesor, contexto);
        return FranjaDisponibilidadVista.de(creada);
    }

    @Override
    @Transactional
    public void liberar(Long idProfesor, String dia, int hora, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        DiaSemana diaSemana = DiaSemana.desdeCodigo(dia);
        DisponibilidadProfesor franja = repositorio.buscar(idProfesor, diaSemana, hora)
                .orElseThrow(() -> new RecursoNoEncontradoException("Disponibilidad", dia + "-" + hora));
        repositorio.eliminar(franja.getId());
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, franja.getId(),
                "Eliminacion de franja " + diaSemana.getEtiqueta() + " " + hora + "h", contexto);
    }

    @Override
    @Transactional
    public int generarRejilla(Long idProfesor, List<String> dias, int horaInicio, int horaFin,
                              ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        exigeProfesor(idProfesor);
        int desde = Math.max(ReglasHorario.HORA_MINIMA, horaInicio);
        int hasta = Math.min(ReglasHorario.HORA_MAXIMA, horaFin);
        if (dias == null || dias.isEmpty() || desde > hasta) {
            throw new com.tesiscalidad.horarios.domain.exception.ValidacionException(
                    "Rango de generacion de rejilla no valido.");
        }
        List<DisponibilidadProfesor> nuevas = new ArrayList<>();
        for (String dia : dias) {
            DiaSemana diaSemana = DiaSemana.desdeCodigo(dia);
            for (int hora = desde; hora <= hasta; hora++) {
                if (repositorio.buscar(idProfesor, diaSemana, hora).isEmpty()) {
                    nuevas.add(DisponibilidadProfesor.crear(idProfesor, diaSemana, hora, OrigenRegistro.GENERADO));
                }
            }
        }
        repositorio.guardarLote(nuevas);
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, idProfesor,
                "Generacion de rejilla: " + nuevas.size() + " franjas", contexto);
        return nuevas.size();
    }

    @Override
    @Transactional
    public void limpiar(Long idProfesor, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        exigeProfesor(idProfesor);
        repositorio.eliminarPorProfesor(idProfesor);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, idProfesor,
                "Limpieza total de disponibilidad del profesor " + idProfesor, contexto);
    }

    private void exigeProfesor(Long idProfesor) {
        profesorRepositorio.buscarPorId(idProfesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor", idProfesor));
    }
}
