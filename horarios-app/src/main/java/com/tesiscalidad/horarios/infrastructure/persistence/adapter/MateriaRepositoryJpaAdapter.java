package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.MateriaEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.MateriaJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador de persistencia de materias sobre JPA/EclipseLink. */
@ApplicationScoped
public class MateriaRepositoryJpaAdapter implements MateriaRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public Optional<Materia> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(MateriaEntity.class, id)).map(MateriaJpaMapper::aDominio);
    }

    @Override
    public Optional<Materia> buscarPorCodigo(String codigo) {
        return em.createQuery(
                        "SELECT m FROM MateriaEntity m WHERE UPPER(m.codigo) = :c", MateriaEntity.class)
                .setParameter("c", codigo == null ? "" : codigo.toUpperCase())
                .getResultStream().findFirst().map(MateriaJpaMapper::aDominio);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM MateriaEntity m WHERE UPPER(m.codigo) = :c", Long.class)
                .setParameter("c", codigo == null ? "" : codigo.toUpperCase())
                .getSingleResult() > 0;
    }

    @Override
    public Materia guardar(Materia materia) {
        MateriaEntity entidad;
        if (materia.getId() == null) {
            entidad = MateriaJpaMapper.aEntidadNueva(materia);
            em.persist(entidad);
        } else {
            entidad = em.find(MateriaEntity.class, materia.getId());
            MateriaJpaMapper.volcar(materia, entidad);
            entidad = em.merge(entidad);
        }
        em.flush();
        return MateriaJpaMapper.aDominio(entidad);
    }

    @Override
    public Pagina<Materia> listar(String texto, Integer semestre, Paginacion paginacion) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (tieneTexto(texto)) {
            where.append(" AND (LOWER(m.nombre) LIKE :t OR LOWER(m.codigo) LIKE :t)");
        }
        if (semestre != null) {
            where.append(" AND m.semestre = :s");
        }
        TypedQuery<Long> conteo = em.createQuery(
                "SELECT COUNT(m) FROM MateriaEntity m" + where, Long.class);
        TypedQuery<MateriaEntity> datos = em.createQuery(
                "SELECT m FROM MateriaEntity m" + where + " ORDER BY m.semestre, m.codigo", MateriaEntity.class);
        aplicarParametros(conteo, datos, texto, semestre);

        long total = conteo.getSingleResult();
        List<Materia> contenido = datos.setFirstResult(paginacion.offset())
                .setMaxResults(paginacion.tamano())
                .getResultList().stream().map(MateriaJpaMapper::aDominio).collect(Collectors.toList());
        return new Pagina<>(contenido, paginacion.pagina(), paginacion.tamano(), total);
    }

    @Override
    public List<Materia> listarActivas() {
        return new ArrayList<>(em.createQuery(
                        "SELECT m FROM MateriaEntity m WHERE m.activa = TRUE ORDER BY m.semestre, m.codigo",
                        MateriaEntity.class)
                .getResultList().stream().map(MateriaJpaMapper::aDominio).collect(Collectors.toList()));
    }

    @Override
    public void eliminar(Long id) {
        MateriaEntity entidad = em.find(MateriaEntity.class, id);
        if (entidad != null) {
            em.remove(entidad);
        }
    }

    private void aplicarParametros(TypedQuery<Long> conteo, TypedQuery<MateriaEntity> datos,
                                   String texto, Integer semestre) {
        if (tieneTexto(texto)) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            conteo.setParameter("t", patron);
            datos.setParameter("t", patron);
        }
        if (semestre != null) {
            conteo.setParameter("s", semestre);
            datos.setParameter("s", semestre);
        }
    }

    private boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
