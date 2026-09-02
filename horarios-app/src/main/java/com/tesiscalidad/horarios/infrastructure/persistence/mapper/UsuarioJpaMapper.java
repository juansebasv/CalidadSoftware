package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.UsuarioEntity;

/** Conversion {@link UsuarioEntity} &lt;-&gt; {@link Usuario}. */
public final class UsuarioJpaMapper {

    private UsuarioJpaMapper() {
    }

    public static Usuario aDominio(UsuarioEntity e) {
        return Usuario.reconstruir(e.getId(), e.getLogin(), e.getNombre(),
                ClaveHash.deHashExistente(e.getClave()), RolUsuario.desdeCodigo(e.getTipo()),
                e.isActivo(), e.getIntentosFallidos(),
                TiempoJpa.aInstant(e.getBloqueadoHasta()), TiempoJpa.aInstant(e.getUltimoAcceso()));
    }

    /** Vuelca el dominio sobre una entidad (nueva o gestionada). */
    public static void volcar(Usuario u, UsuarioEntity e) {
        e.setLogin(u.getLogin());
        e.setNombre(u.getNombre());
        e.setClave(u.getClave().valor());
        e.setTipo(u.getRol().getCodigo());
        e.setActivo(u.isActivo());
        e.setIntentosFallidos(u.getIntentosFallidos());
        e.setBloqueadoHasta(TiempoJpa.aOffset(u.getBloqueadoHasta()));
        e.setUltimoAcceso(TiempoJpa.aOffset(u.getUltimoAcceso()));
    }

    public static UsuarioEntity aEntidadNueva(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        volcar(u, e);
        return e;
    }
}
