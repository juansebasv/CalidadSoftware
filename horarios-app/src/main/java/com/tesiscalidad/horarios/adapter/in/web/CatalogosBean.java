package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Catalogos estaticos (enums y rangos) para los desplegables de la UI. */
@Named("catalogos")
@ApplicationScoped
public class CatalogosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public TipoMateria[] getTiposMateria() {
        return TipoMateria.values();
    }

    public TipoContrato[] getTiposContrato() {
        return TipoContrato.values();
    }

    public Jornada[] getJornadas() {
        return Jornada.values();
    }

    public RolUsuario[] getRoles() {
        return RolUsuario.values();
    }

    /** Dias habiles (Lunes a Sabado) para la programacion. */
    public List<DiaSemana> getDiasHabiles() {
        List<DiaSemana> dias = new ArrayList<>();
        for (DiaSemana d : DiaSemana.values()) {
            if (d != DiaSemana.DOMINGO) {
                dias.add(d);
            }
        }
        return dias;
    }

    public List<Integer> getHoras() {
        List<Integer> horas = new ArrayList<>();
        for (int h = ReglasHorario.HORA_MINIMA; h <= ReglasHorario.HORA_MAXIMA; h++) {
            horas.add(h);
        }
        return horas;
    }

    public List<Integer> getSemestres() {
        List<Integer> semestres = new ArrayList<>();
        for (int s = ReglasHorario.SEMESTRE_MINIMO; s <= ReglasHorario.SEMESTRE_MAXIMO; s++) {
            semestres.add(s);
        }
        return semestres;
    }

    public List<Integer> getDuraciones() {
        List<Integer> duraciones = new ArrayList<>();
        for (int d = ReglasHorario.DURACION_MINIMA_BLOQUES; d <= ReglasHorario.DURACION_MAXIMA_BLOQUES; d++) {
            duraciones.add(d);
        }
        return duraciones;
    }
}
