package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarHorarioUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarMateriasUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarSesionesUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/** Controlador de la vista de programacion de sesiones de clase. */
@Named("sesionBean")
@ViewScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarSesionesUseCase useCase;

    @Inject
    ConsultarHorarioUseCase consultaUseCase;

    @Inject
    GestionarMateriasUseCase materiasUseCase;

    @Inject
    GestionarProfesoresUseCase profesoresUseCase;

    @Inject
    SesionUsuarioBean sesion;

    private PaginaLazyModel<SesionClaseVista> modelo;
    private Integer filtroSemestre;
    private List<MateriaVista> materias = new ArrayList<>();
    private List<ProfesorVista> profesores = new ArrayList<>();
    private ScheduleModel agenda = new DefaultScheduleModel();

    private Long id;
    private Long idMateria;
    private Long idProfesor;
    private String grupo = "A";
    private String dia = "1";
    private int hora = 6;
    private int duracion = 2;
    private int semestre = 1;
    private String jornada = "D";
    private boolean edicion;

    @PostConstruct
    public void init() {
        this.modelo = new PaginaLazyModel<>(
                (pagina, tamano, texto) -> useCase.listar(filtroSemestre, pagina, tamano));
        this.materias = materiasUseCase.listarActivas();
        this.profesores = profesoresUseCase.listarActivos();
        reconstruirAgenda();
    }

    private void reconstruirAgenda() {
        DefaultScheduleModel nueva = new DefaultScheduleModel();
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (SesionClaseVista s : consultaUseCase.horarioPorSemestre(
                filtroSemestre == null ? 1 : filtroSemestre, null)) {
            LocalDate fecha = lunes.plusDays(Integer.parseInt(s.getDia()) - 1L);
            LocalDateTime inicio = fecha.atTime(s.getHora(), 0);
            LocalDateTime fin = fecha.atTime(s.getHoraFin(), 0);
            DefaultScheduleEvent evento = DefaultScheduleEvent.builder()
                    .title(s.getCodigoMateria() + " G" + s.getGrupo() + " - " + s.getNombreProfesor())
                    .startDate(inicio)
                    .endDate(fin)
                    .description(s.getNombreMateria())
                    .build();
            nueva.addEvent(evento);
        }
        this.agenda = nueva;
    }

    public void aplicarFiltro() {
        reconstruirAgenda();
    }

    public void nuevo() {
        id = null;
        idMateria = null;
        idProfesor = null;
        grupo = "A";
        dia = "1";
        hora = 6;
        duracion = 2;
        semestre = 1;
        jornada = "D";
        edicion = false;
    }

    public void editar(SesionClaseVista s) {
        id = s.getId();
        idMateria = s.getIdMateria();
        idProfesor = s.getIdProfesor();
        grupo = s.getGrupo();
        dia = s.getDia();
        hora = s.getHora();
        duracion = s.getDuracion();
        semestre = s.getSemestre();
        jornada = s.getJornada();
        edicion = true;
    }

    public void guardar() {
        try {
            if (edicion) {
                useCase.reprogramar(id, new ReprogramarSesionComando(dia, hora, duracion, jornada),
                        sesion.contexto());
                if (idProfesor != null) {
                    useCase.asignarDocente(id, idProfesor, sesion.contexto());
                }
                Mensajes.exito("Sesion reprogramada", "");
            } else {
                useCase.programar(new ProgramarSesionComando(idMateria, idProfesor, grupo, dia, hora,
                        duracion, semestre, jornada), sesion.contexto());
                Mensajes.exito("Sesion programada", "");
            }
            reconstruirAgenda();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo programar", ex.getMessage());
        }
    }

    public void quitarDocente(SesionClaseVista s) {
        try {
            useCase.quitarDocente(s.getId(), sesion.contexto());
            Mensajes.exito("Docente retirado", "");
            reconstruirAgenda();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo actualizar", ex.getMessage());
        }
    }

    public void cancelar(SesionClaseVista s) {
        try {
            useCase.cancelar(s.getId(), sesion.contexto());
            Mensajes.exito("Sesion cancelada", "");
            reconstruirAgenda();
        } catch (DominioException ex) {
            Mensajes.error("No se pudo cancelar", ex.getMessage());
        }
    }

    public PaginaLazyModel<SesionClaseVista> getModelo() {
        return modelo;
    }

    public ScheduleModel getAgenda() {
        return agenda;
    }

    public List<MateriaVista> getMaterias() {
        return materias;
    }

    public List<ProfesorVista> getProfesores() {
        return profesores;
    }

    public Integer getFiltroSemestre() {
        return filtroSemestre;
    }

    public void setFiltroSemestre(Integer filtroSemestre) {
        this.filtroSemestre = filtroSemestre;
    }

    public Long getId() {
        return id;
    }

    public Long getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Long idMateria) {
        this.idMateria = idMateria;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(Long idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public boolean isEdicion() {
        return edicion;
    }
}
