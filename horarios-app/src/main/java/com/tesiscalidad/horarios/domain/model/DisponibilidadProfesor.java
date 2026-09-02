package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.vo.Franja;

/** Una celda de la rejilla de disponibilidad semanal de un profesor. */
public final class DisponibilidadProfesor {

    private final Long id;
    private final Long idProfesor;
    private final DiaSemana dia;
    private final int hora;
    private EstadoFranja estado;
    private OrigenRegistro origen;

    private DisponibilidadProfesor(Long id, Long idProfesor, DiaSemana dia, int hora,
                                   EstadoFranja estado, OrigenRegistro origen) {
        this.id = id;
        this.idProfesor = idProfesor;
        this.dia = dia;
        this.hora = hora;
        this.estado = estado;
        this.origen = origen;
    }

    public static DisponibilidadProfesor crear(Long idProfesor, DiaSemana dia, int hora, OrigenRegistro origen) {
        Franja.puntual(dia, hora); // valida dia + rango de hora
        return new DisponibilidadProfesor(null,
                Preconditions.requerido(idProfesor, "idProfesor"),
                dia, hora, EstadoFranja.LIBRE,
                Preconditions.requerido(origen, "origen"));
    }

    public static DisponibilidadProfesor reconstruir(Long id, Long idProfesor, DiaSemana dia, int hora,
                                                     EstadoFranja estado, OrigenRegistro origen) {
        Franja.puntual(dia, hora);
        return new DisponibilidadProfesor(Preconditions.requerido(id, "id"),
                Preconditions.requerido(idProfesor, "idProfesor"), dia, hora,
                Preconditions.requerido(estado, "estado"),
                Preconditions.requerido(origen, "origen"));
    }

    public void ocupar() {
        this.estado = EstadoFranja.OCUPADA;
    }

    public void liberar() {
        this.estado = EstadoFranja.LIBRE;
    }

    public boolean estaLibre() {
        return estado.estaLibre();
    }

    public Franja comoFranja() {
        return Franja.puntual(dia, hora);
    }

    public Long getId() {
        return id;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public DiaSemana getDia() {
        return dia;
    }

    public int getHora() {
        return hora;
    }

    public EstadoFranja getEstado() {
        return estado;
    }

    public OrigenRegistro getOrigen() {
        return origen;
    }
}
