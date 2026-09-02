package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.ProfesorEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.ProfesorJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador de persistencia de profesores sobre JPA/EclipseLink. */
@ApplicationScoped
public class ProfesorRepositoryJpaAdapter implements ProfesorRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public Optional<Profesor> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(ProfesorEntity.class, id)).map(ProfesorJpaMapper::aDominio);
    }

    @Override
    public Optional<Profesor> buscarPorCodigo(String codigo) {
        return em.createQuery(
                        "SELECT p FROM ProfesorEntity p WHERE UPPER(p.codigo) = :c", ProfesorEntity.class)
                .setParameter("c", codigo == null ? "" : codigo.toUpperCase())
                .getResultStream().findFirst().map(ProfesorJpaMapper::aDominio);
    }

    @Override
    public Optional<Profesor> buscarPorIdUsuario(Long idUsuario) {
        if (idUsuario == null) {
            return Optional.empty();
        }
        return em.createQuery(
                        "SELECT p FROM ProfesorEntity p WHERE p.idUsuario = :u", ProfesorEntity.class)
                .setParameter("u", idUsuario)
                .getResultStream().findFirst().map(ProfesorJpaMapper::aDominio);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return em.createQuery(
                        "SELECT COUNT(p) FROM ProfesorEntity p WHERE UPPER(p.codigo) = :c", Long.class)
                .setParameter("c", codigo == null ? "" : codigo.toUpperCase())
                .getSingleResult() > 0;
    }

    @Override
    public Profesor guardar(Profesor profesor) {
        ProfesorEntity entidad;
        if (profesor.getId() == null) {
            entidad = ProfesorJpaMapper.aEntidadNueva(profesor);
            em.persist(entidad);
        } else {
            entidad = em.find(ProfesorEntity.class, profesor.getId());
            ProfesorJpaMapper.volcar(profesor, entidad);
            entidad = em.merge(entidad);
        }
        em.flush();
        return ProfesorJpaMapper.aDominio(entidad);
    }

    @Override
    public Pagina<Profesor> listar(String texto, Paginacion paginacion) {
        boolean filtra = texto != null && !texto.isBlank();
        String where = filtra ? " WHERE LOWER(p.nombre) LIKE :t OR LOWER(p.codigo) LIKE :t" : "";
        var conteo = em.createQuery("SELECT COUNT(p) FROM ProfesorEntity p" + where, Long.class);
        var datos = em.createQuery("SELECT p FROM ProfesorEntity p" + where + " ORDER BY p.nombre",
                ProfesorEntity.class);
        if (filtra) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            conteo.setParameter("t", patron);
            datos.setParameter("t", patron);
        }
        long total = conteo.getSingleResult();
        List<Profesor> contenido = datos.setFirstResult(paginacion.offset())
                .setMaxResults(paginacion.tamano())
                .getResultList().stream().map(ProfesorJpaMapper::aDominio).collect(Collectors.toList());
        return new Pagina<>(contenido, paginacion.pagina(), paginacion.tamano(), total);
    }

    @Override
    public List<Profesor> listarActivos() {
        return em.createQuery(
                        "SELECT p FROM ProfesorEntity p WHERE p.activo = TRUE ORDER BY p.nombre",
                        ProfesorEntity.class)
                .getResultList().stream().map(ProfesorJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        ProfesorEntity entidad = em.find(ProfesorEntity.class, id);
        if (entidad != null) {
            em.remove(entidad);
        }
    }
}
