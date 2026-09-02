package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.disponibilidad.FranjaDisponibilidadVista;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.port.in.GestionarDisponibilidadUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.exception.DominioException;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Controlador de la vista de disponibilidad docente (rejilla semanal). */
@Named("disponibilidadBean")
@ViewScoped
public class DisponibilidadBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarDisponibilidadUseCase useCase;

    @Inject
    GestionarProfesoresUseCase profesoresUseCase;

    @Inject
    SesionUsuarioBean sesion;

    private List<ProfesorVista> profesores = new ArrayList<>();
    private Long idProfesor;
    private List<FranjaDisponibilidadVista> franjas = new ArrayList<>();

    private List<String> diasGeneracion = new ArrayList<>(List.of("1", "2", "3", "4", "5"));
    private int horaInicioGeneracion = ReglasHorario.HORA_MINIMA;
    private int horaFinGeneracion = 18;

    @PostConstruct
    public void init() {
        this.profesores = profesoresUseCase.listarActivos();
    }

    public void cargar() {
        this.franjas = idProfesor == null ? new ArrayList<>() : useCase.listar(idProfesor);
    }

    /** {@code true} si el profesor tiene la franja (dia,hora) marcada. */
    public boolean tiene(String dia, int hora) {
        return franjas.stream().anyMatch(f -> f.getDia().equals(dia) && f.getHora() == hora);
    }

    public String estado(String dia, int hora) {
        return franjas.stream()
                .filter(f -> f.getDia().equals(dia) && f.getHora() == hora)
                .map(FranjaDisponibilidadVista::getEstado)
                .findFirst().orElse("");
    }

    public void alternar(String dia, int hora) {
        if (idProfesor == null) {
            Mensajes.advertencia("Seleccione un profesor", "");
            return;
        }
        try {
            if (tiene(dia, hora)) {
                useCase.liberar(idProfesor, dia, hora, sesion.contexto());
            } else {
                useCase.marcar(idProfesor, dia, hora, sesion.contexto());
            }
            cargar();
        } catch (DominioException ex) {
            Mensajes.error("Operacion rechazada", ex.getMessage());
        }
    }

    public void generar() {
        try {
            int creadas = useCase.generarRejilla(idProfesor, diasGeneracion,
                    horaInicioGeneracion, horaFinGeneracion, sesion.contexto());
            Mensajes.exito("Rejilla generada", creadas + " franja(s) nueva(s)");
            cargar();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo generar", ex.getMessage());
        }
    }

    public void limpiar() {
        try {
            useCase.limpiar(idProfesor, sesion.contexto());
            Mensajes.exito("Disponibilidad eliminada", "");
            cargar();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo limpiar", ex.getMessage());
        }
    }

    public List<String[]> getDiasRejilla() {
        List<String[]> dias = new ArrayList<>();
        for (DiaSemana d : DiaSemana.values()) {
            if (d != DiaSemana.DOMINGO) {
                dias.add(new String[]{d.getCodigo(), d.getEtiqueta()});
            }
        }
        return dias;
    }

    public List<Integer> getHorasRejilla() {
        List<Integer> horas = new ArrayList<>();
        for (int h = ReglasHorario.HORA_MINIMA; h <= ReglasHorario.HORA_MAXIMA; h++) {
            horas.add(h);
        }
        return horas;
    }

    public List<ProfesorVista> getProfesores() {
        return profesores;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(Long idProfesor) {
        this.idProfesor = idProfesor;
    }

    public List<FranjaDisponibilidadVista> getFranjas() {
        return franjas;
    }

    public List<String> getDiasGeneracion() {
        return diasGeneracion;
    }

    public void setDiasGeneracion(List<String> diasGeneracion) {
        this.diasGeneracion = diasGeneracion;
    }

    public int getHoraInicioGeneracion() {
        return horaInicioGeneracion;
    }

    public void setHoraInicioGeneracion(int horaInicioGeneracion) {
        this.horaInicioGeneracion = horaInicioGeneracion;
    }

    public int getHoraFinGeneracion() {
        return horaFinGeneracion;
    }

    public void setHoraFinGeneracion(int horaFinGeneracion) {
        this.horaFinGeneracion = horaFinGeneracion;
    }
}
