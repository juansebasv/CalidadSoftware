package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.UsuarioEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.TiempoJpa;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.UsuarioJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador de persistencia de usuarios sobre JPA/EclipseLink. */
@ApplicationScoped
public class UsuarioRepositoryJpaAdapter implements UsuarioRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(UsuarioEntity.class, id)).map(UsuarioJpaMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return em.createQuery(
                        "SELECT u FROM UsuarioEntity u WHERE LOWER(u.login) = :l", UsuarioEntity.class)
                .setParameter("l", login == null ? "" : login.toLowerCase())
                .getResultStream().findFirst().map(UsuarioJpaMapper::aDominio);
    }

    @Override
    public boolean existeLogin(String login) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM UsuarioEntity u WHERE LOWER(u.login) = :l", Long.class)
                .setParameter("l", login == null ? "" : login.toLowerCase())
                .getSingleResult() > 0;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entidad;
        if (usuario.getId() == null) {
            entidad = UsuarioJpaMapper.aEntidadNueva(usuario);
            em.persist(entidad);
        } else {
            entidad = em.find(UsuarioEntity.class, usuario.getId());
            UsuarioJpaMapper.volcar(usuario, entidad);
            entidad = em.merge(entidad);
        }
        em.flush();
        return UsuarioJpaMapper.aDominio(entidad);
    }

    /**
     * Transaccion nueva: el contador de intentos y el bloqueo deben persistir
     * aunque el caso de uso de autenticacion termine lanzando una excepcion.
     */
    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void actualizarEstadoAcceso(Usuario usuario) {
        em.createQuery("UPDATE UsuarioEntity u SET u.intentosFallidos = :i, u.bloqueadoHasta = :b, "
                        + "u.ultimoAcceso = :a WHERE u.id = :id")
                .setParameter("i", usuario.getIntentosFallidos())
                .setParameter("b", TiempoJpa.aOffset(usuario.getBloqueadoHasta()))
                .setParameter("a", TiempoJpa.aOffset(usuario.getUltimoAcceso()))
                .setParameter("id", usuario.getId())
                .executeUpdate();
    }

    @Override
    public Pagina<Usuario> listar(String texto, Paginacion paginacion) {
        boolean filtra = texto != null && !texto.isBlank();
        String where = filtra ? " WHERE LOWER(u.login) LIKE :t OR LOWER(u.nombre) LIKE :t" : "";
        var conteo = em.createQuery("SELECT COUNT(u) FROM UsuarioEntity u" + where, Long.class);
        var datos = em.createQuery("SELECT u FROM UsuarioEntity u" + where + " ORDER BY u.login",
                UsuarioEntity.class);
        if (filtra) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            conteo.setParameter("t", patron);
            datos.setParameter("t", patron);
        }
        long total = conteo.getSingleResult();
        List<Usuario> contenido = datos.setFirstResult(paginacion.offset())
                .setMaxResults(paginacion.tamano())
                .getResultList().stream().map(UsuarioJpaMapper::aDominio).collect(Collectors.toList());
        return new Pagina<>(contenido, paginacion.pagina(), paginacion.tamano(), total);
    }

    @Override
    public void eliminar(Long id) {
        UsuarioEntity entidad = em.find(UsuarioEntity.class, id);
        if (entidad != null) {
            em.remove(entidad);
        }
    }
}
