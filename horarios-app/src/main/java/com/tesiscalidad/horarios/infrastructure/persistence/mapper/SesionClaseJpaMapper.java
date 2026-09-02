package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.SesionClaseEntity;

/** Conversion {@link SesionClaseEntity} &lt;-&gt; {@link SesionClase}. */
public final class SesionClaseJpaMapper {

    private SesionClaseJpaMapper() {
    }

    public static SesionClase aDominio(SesionClaseEntity e) {
        return SesionClase.reconstruir(e.getId(), e.getIdMateria(), e.getIdProfesor(), e.getGrupo(),
                DiaSemana.desdeCodigo(e.getDia()), Integer.parseInt(e.getHora()), e.getDuracion(),
                e.getSemestre(), Jornada.desdeCodigo(e.getJornada()));
    }

    public static void volcar(SesionClase s, SesionClaseEntity e) {
        e.setIdMateria(s.getIdMateria());
        e.setIdProfesor(s.getIdProfesor());
        e.setGrupo(s.getGrupo());
        e.setDia(s.getDia().getCodigo());
        e.setHora(String.format("%02d", s.getHora()));
        e.setDuracion(s.getDuracion());
        e.setSemestre(s.getSemestre());
        e.setJornada(s.getJornada().getCodigo());
    }

    public static SesionClaseEntity aEntidadNueva(SesionClase s) {
        SesionClaseEntity e = new SesionClaseEntity();
        volcar(s, e);
        return e;
    }
}
