package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.MateriaEntity;

/** Conversion {@link MateriaEntity} &lt;-&gt; {@link Materia}. */
public final class MateriaJpaMapper {

    private MateriaJpaMapper() {
    }

    public static Materia aDominio(MateriaEntity e) {
        return Materia.reconstruir(e.getId(), e.getCodigo(), e.getNombre(),
                TipoMateria.desdeCodigo(e.getTipo()), e.getCreditos(), e.getIntensidadHoraria(),
                e.getSemestre(), e.isActiva());
    }

    public static void volcar(Materia m, MateriaEntity e) {
        e.setCodigo(m.getCodigo().valor());
        e.setNombre(m.getNombre());
        e.setTipo(m.getTipo().getCodigo());
        e.setCreditos(m.getCreditos());
        e.setIntensidadHoraria(m.getIntensidadHoraria());
        e.setSemestre(m.getSemestre());
        e.setActiva(m.isActiva());
    }

    public static MateriaEntity aEntidadNueva(Materia m) {
        MateriaEntity e = new MateriaEntity();
        volcar(m, e);
        return e;
    }
}
