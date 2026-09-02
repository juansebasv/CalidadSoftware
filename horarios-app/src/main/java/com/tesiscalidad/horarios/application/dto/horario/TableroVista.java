package com.tesiscalidad.horarios.application.dto.horario;

import java.util.List;

/** Datos agregados para el tablero (dashboard) principal. */
public final class TableroVista {

    private final long totalMaterias;
    private final long totalProfesores;
    private final long totalUsuarios;
    private final long totalSesiones;
    private final long sesionesSinDocente;
    private final long totalConflictos;
    private final List<ConteoVista> sesionesPorDia;
    private final List<ConteoVista> sesionesPorSemestre;
    private final List<ConteoVista> materiasPorTipo;
    private final List<ConteoVista> profesoresPorContrato;

    @SuppressWarnings("java:S107")
    public TableroVista(long totalMaterias, long totalProfesores, long totalUsuarios, long totalSesiones,
                        long sesionesSinDocente, long totalConflictos, List<ConteoVista> sesionesPorDia,
                        List<ConteoVista> sesionesPorSemestre, List<ConteoVista> materiasPorTipo,
                        List<ConteoVista> profesoresPorContrato) {
        this.totalMaterias = totalMaterias;
        this.totalProfesores = totalProfesores;
        this.totalUsuarios = totalUsuarios;
        this.totalSesiones = totalSesiones;
        this.sesionesSinDocente = sesionesSinDocente;
        this.totalConflictos = totalConflictos;
        this.sesionesPorDia = List.copyOf(sesionesPorDia);
        this.sesionesPorSemestre = List.copyOf(sesionesPorSemestre);
        this.materiasPorTipo = List.copyOf(materiasPorTipo);
        this.profesoresPorContrato = List.copyOf(profesoresPorContrato);
    }

    public long getTotalMaterias() {
        return totalMaterias;
    }

    public long getTotalProfesores() {
        return totalProfesores;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public long getTotalSesiones() {
        return totalSesiones;
    }

    public long getSesionesSinDocente() {
        return sesionesSinDocente;
    }

    public long getTotalConflictos() {
        return totalConflictos;
    }

    public List<ConteoVista> getSesionesPorDia() {
        return sesionesPorDia;
    }

    public List<ConteoVista> getSesionesPorSemestre() {
        return sesionesPorSemestre;
    }

    public List<ConteoVista> getMateriasPorTipo() {
        return materiasPorTipo;
    }

    public List<ConteoVista> getProfesoresPorContrato() {
        return profesoresPorContrato;
    }
}
