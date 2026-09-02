package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.port.in.GestionarPerfilesUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import org.primefaces.model.DualListModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Controlador de la vista de perfiles (materias habilitadas por profesor). */
@Named("perfilBean")
@ViewScoped
public class PerfilBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarPerfilesUseCase perfilesUseCase;

    @Inject
    GestionarProfesoresUseCase profesoresUseCase;

    @Inject
    SesionUsuarioBean sesion;

    private List<ProfesorVista> profesores = new ArrayList<>();
    private Long idProfesorSeleccionado;
    private DualListModel<MateriaVista> materias = new DualListModel<>(new ArrayList<>(), new ArrayList<>());

    @PostConstruct
    public void init() {
        this.profesores = profesoresUseCase.listarActivos();
    }

    public void cargarMaterias() {
        if (idProfesorSeleccionado == null) {
            this.materias = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
            return;
        }
        List<MateriaVista> disponibles = perfilesUseCase.materiasDisponibles(idProfesorSeleccionado);
        List<MateriaVista> habilitadas = perfilesUseCase.materiasHabilitadas(idProfesorSeleccionado);
        this.materias = new DualListModel<>(disponibles, habilitadas);
    }

    /** Sincroniza la BD con el estado del pickList (altas y bajas). */
    public void guardar() {
        if (idProfesorSeleccionado == null) {
            Mensajes.advertencia("Seleccione un profesor", "");
            return;
        }
        try {
            List<MateriaVista> objetivo = materias.getTarget();
            List<Long> idsObjetivo = objetivo.stream().map(MateriaVista::getId).collect(java.util.stream.Collectors.toList());
            List<Long> idsActuales = perfilesUseCase.materiasHabilitadas(idProfesorSeleccionado)
                    .stream().map(MateriaVista::getId).collect(java.util.stream.Collectors.toList());

            idsObjetivo.stream().filter(id -> !idsActuales.contains(id))
                    .forEach(id -> perfilesUseCase.habilitar(idProfesorSeleccionado, id, sesion.contexto()));
            idsActuales.stream().filter(id -> !idsObjetivo.contains(id))
                    .forEach(id -> perfilesUseCase.deshabilitar(idProfesorSeleccionado, id, sesion.contexto()));

            Mensajes.exito("Perfiles actualizados", objetivo.size() + " materia(s) habilitada(s)");
            cargarMaterias();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo guardar", ex.getMessage());
            cargarMaterias();
        }
    }

    public List<ProfesorVista> getProfesores() {
        return profesores;
    }

    public Long getIdProfesorSeleccionado() {
        return idProfesorSeleccionado;
    }

    public void setIdProfesorSeleccionado(Long idProfesorSeleccionado) {
        this.idProfesorSeleccionado = idProfesorSeleccionado;
    }

    public DualListModel<MateriaVista> getMaterias() {
        return materias;
    }

    public void setMaterias(DualListModel<MateriaVista> materias) {
        this.materias = materias;
    }
}
