package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.horario.ConflictoVista;
import com.tesiscalidad.horarios.application.dto.horario.ConteoVista;
import com.tesiscalidad.horarios.application.dto.horario.TableroVista;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarHorarioUseCase;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;

import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Controlador del tablero (dashboard) y de las consultas de horario. */
@Named("horarioBean")
@ViewScoped
public class HorarioBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final List<String> PALETA = Arrays.asList(
            "#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#EC407A", "#26C6DA", "#D4E157", "#8D6E63");

    @Inject
    ConsultarHorarioUseCase useCase;

    @Inject
    GestionarProfesoresUseCase profesoresUseCase;

    private TableroVista tablero;
    private BarChartModel sesionesPorDia;
    private BarChartModel sesionesPorSemestre;
    private PieChartModel materiasPorTipo;
    private DonutChartModel profesoresPorContrato;

    private List<ConflictoVista> conflictos = new ArrayList<>();
    private List<ProfesorVista> profesores = new ArrayList<>();
    private Integer semestreConsulta = 1;
    private String jornadaConsulta;
    private Long idProfesorConsulta;
    private List<SesionClaseVista> resultadoConsulta = new ArrayList<>();
    private ScheduleModel agenda = new DefaultScheduleModel();

    @PostConstruct
    public void init() {
        this.tablero = useCase.tablero();
        this.conflictos = useCase.detectarConflictos();
        this.profesores = profesoresUseCase.listarActivos();
        this.sesionesPorDia = barra(tablero.getSesionesPorDia(), "Sesiones por dia");
        this.sesionesPorSemestre = barra(tablero.getSesionesPorSemestre(), "Sesiones por semestre");
        this.materiasPorTipo = pastel(tablero.getMateriasPorTipo());
        this.profesoresPorContrato = dona(tablero.getProfesoresPorContrato());
        consultarPorSemestre();
    }

    public void consultarPorSemestre() {
        this.resultadoConsulta = useCase.horarioPorSemestre(
                semestreConsulta == null ? 1 : semestreConsulta, jornadaConsulta);
        reconstruirAgenda(resultadoConsulta);
    }

    public void consultarPorProfesor() {
        if (idProfesorConsulta == null) {
            this.resultadoConsulta = new ArrayList<>();
        } else {
            this.resultadoConsulta = useCase.horarioDeProfesor(idProfesorConsulta);
        }
        reconstruirAgenda(resultadoConsulta);
    }

    private void reconstruirAgenda(List<SesionClaseVista> sesiones) {
        DefaultScheduleModel modelo = new DefaultScheduleModel();
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (SesionClaseVista s : sesiones) {
            LocalDate fecha = lunes.plusDays(Integer.parseInt(s.getDia()) - 1L);
            modelo.addEvent(DefaultScheduleEvent.builder()
                    .title(s.getCodigoMateria() + " G" + s.getGrupo())
                    .startDate(fecha.atTime(s.getHora(), 0))
                    .endDate(fecha.atTime(s.getHoraFin(), 0))
                    .description(s.getNombreMateria() + " - " + s.getNombreProfesor())
                    .build());
        }
        this.agenda = modelo;
    }

    private static Legend leyendaCompacta(boolean visible) {
        Legend legend = new Legend();
        legend.setDisplay(visible);
        legend.setPosition("bottom");
        return legend;
    }

    private BarChartModel barra(List<ConteoVista> datos, String etiqueta) {
        BarChartModel modelo = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet ds = new BarChartDataSet();
        ds.setLabel(etiqueta);
        ds.setData(datos.stream().map(c -> (Number) c.getValor()).collect(Collectors.toList()));
        ds.setBackgroundColor(PALETA);
        data.addChartDataSet(ds);
        data.setLabels(datos.stream().map(ConteoVista::getEtiqueta).collect(Collectors.toList()));
        modelo.setData(data);

        BarChartOptions opciones = new BarChartOptions();
        opciones.setLegend(leyendaCompacta(false));
        modelo.setOptions(opciones);
        return modelo;
    }

    private PieChartModel pastel(List<ConteoVista> datos) {
        PieChartModel modelo = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet ds = new PieChartDataSet();
        ds.setData(datos.stream().map(c -> (Number) c.getValor()).collect(Collectors.toList()));
        ds.setBackgroundColor(PALETA);
        data.addChartDataSet(ds);
        data.setLabels(datos.stream().map(ConteoVista::getEtiqueta).collect(Collectors.toList()));
        modelo.setData(data);

        PieChartOptions opciones = new PieChartOptions();
        opciones.setLegend(leyendaCompacta(true));
        modelo.setOptions(opciones);
        return modelo;
    }

    private DonutChartModel dona(List<ConteoVista> datos) {
        DonutChartModel modelo = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet ds = new DonutChartDataSet();
        ds.setData(datos.stream().map(c -> (Number) c.getValor()).collect(Collectors.toList()));
        ds.setBackgroundColor(PALETA);
        data.addChartDataSet(ds);
        data.setLabels(datos.stream().map(ConteoVista::getEtiqueta).collect(Collectors.toList()));
        modelo.setData(data);

        org.primefaces.model.charts.donut.DonutChartOptions opciones =
                new org.primefaces.model.charts.donut.DonutChartOptions();
        opciones.setLegend(leyendaCompacta(true));
        modelo.setOptions(opciones);
        return modelo;
    }

    public TableroVista getTablero() {
        return tablero;
    }

    public BarChartModel getSesionesPorDia() {
        return sesionesPorDia;
    }

    public BarChartModel getSesionesPorSemestre() {
        return sesionesPorSemestre;
    }

    public PieChartModel getMateriasPorTipo() {
        return materiasPorTipo;
    }

    public DonutChartModel getProfesoresPorContrato() {
        return profesoresPorContrato;
    }

    public List<ConflictoVista> getConflictos() {
        return conflictos;
    }

    public List<ProfesorVista> getProfesores() {
        return profesores;
    }

    public Integer getSemestreConsulta() {
        return semestreConsulta;
    }

    public void setSemestreConsulta(Integer semestreConsulta) {
        this.semestreConsulta = semestreConsulta;
    }

    public String getJornadaConsulta() {
        return jornadaConsulta;
    }

    public void setJornadaConsulta(String jornadaConsulta) {
        this.jornadaConsulta = jornadaConsulta;
    }

    public Long getIdProfesorConsulta() {
        return idProfesorConsulta;
    }

    public void setIdProfesorConsulta(Long idProfesorConsulta) {
        this.idProfesorConsulta = idProfesorConsulta;
    }

    public List<SesionClaseVista> getResultadoConsulta() {
        return resultadoConsulta;
    }

    public ScheduleModel getAgenda() {
        return agenda;
    }
}
