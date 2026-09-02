package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.DisponibilidadProfesorEntity;

/** Conversion {@link DisponibilidadProfesorEntity} &lt;-&gt; {@link DisponibilidadProfesor}. */
public final class DisponibilidadJpaMapper {

    private DisponibilidadJpaMapper() {
    }

    public static DisponibilidadProfesor aDominio(DisponibilidadProfesorEntity e) {
        return DisponibilidadProfesor.reconstruir(e.getId(), e.getIdProfesor(),
                DiaSemana.desdeCodigo(e.getDia()), Integer.parseInt(e.getHora()),
                EstadoFranja.desdeCodigo(e.getEstado()), OrigenRegistro.desdeCodigo(e.getManual()));
    }

    public static void volcar(DisponibilidadProfesor d, DisponibilidadProfesorEntity e) {
        e.setIdProfesor(d.getIdProfesor());
        e.setDia(d.getDia().getCodigo());
        e.setHora(String.format("%02d", d.getHora()));
        e.setEstado(d.getEstado().getCodigo());
        e.setManual(d.getOrigen().getCodigo());
    }

    public static DisponibilidadProfesorEntity aEntidadNueva(DisponibilidadProfesor d) {
        DisponibilidadProfesorEntity e = new DisponibilidadProfesorEntity();
        volcar(d, e);
        return e;
    }
}
