package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.ProfesorEntity;

/** Conversion {@link ProfesorEntity} &lt;-&gt; {@link Profesor}. */
public final class ProfesorJpaMapper {

    private static final String DISPONIBLE = "1";

    private ProfesorJpaMapper() {
    }

    public static Profesor aDominio(ProfesorEntity e) {
        return Profesor.reconstruir(e.getId(), e.getCodigo(), e.getNombre(),
                TipoContrato.desdeCodigo(e.getTipoContrato()),
                DISPONIBLE.equals(e.getDisponibilidad()), e.getIdUsuario(), e.isActivo());
    }

    public static void volcar(Profesor p, ProfesorEntity e) {
        e.setCodigo(p.getCodigo().valor());
        e.setNombre(p.getNombre());
        e.setTipoContrato(p.getTipoContrato().getCodigo());
        e.setDisponibilidad(p.isDisponible() ? "1" : "0");
        e.setIdUsuario(p.getIdUsuario());
        e.setActivo(p.isActivo());
    }

    public static ProfesorEntity aEntidadNueva(Profesor p) {
        ProfesorEntity e = new ProfesorEntity();
        volcar(p, e);
        return e;
    }
}
