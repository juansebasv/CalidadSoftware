package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.SesionClaseEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.SesionClaseJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador de persistencia de las sesiones de clase. */
@ApplicationScoped
public class SesionClaseRepositoryJpaAdapter implements SesionClaseRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public Optional<SesionClase> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(SesionClaseEntity.class, id)).map(SesionClaseJpaMapper::aDominio);
    }

    @Override
    public boolean existeMateriaGrupoDia(Long idMateria, String grupo, DiaSemana dia, Long idExcluir) {
        String jpql = "SELECT COUNT(s) FROM SesionClaseEntity s WHERE s.idMateria = :m "
                + "AND UPPER(s.grupo) = :g AND s.dia = :d";
        if (idExcluir != null) {
            jpql += " AND s.id <> :ex";
        }
        var query = em.createQuery(jpql, Long.class)
                .setParameter("m", idMateria)
                .setParameter("g", grupo == null ? "" : grupo.toUpperCase())
                .setParameter("d", dia.getCodigo());
        if (idExcluir != null) {
            query.setParameter("ex", idExcluir);
        }
        return query.getSingleResult() > 0;
    }

    @Override
    public SesionClase guardar(SesionClase sesion) {
        SesionClaseEntity entidad;
        if (sesion.getId() == null) {
            entidad = SesionClaseJpaMapper.aEntidadNueva(sesion);
            em.persist(entidad);
        } else {
            entidad = em.find(SesionClaseEntity.class, sesion.getId());
            SesionClaseJpaMapper.volcar(sesion, entidad);
            entidad = em.merge(entidad);
        }
        em.flush();
        return SesionClaseJpaMapper.aDominio(entidad);
    }

    @Override
    public Pagina<SesionClase> listar(Integer semestre, Paginacion paginacion) {
        String where = semestre == null ? "" : " WHERE s.semestre = :s";
        var conteo = em.createQuery("SELECT COUNT(s) FROM SesionClaseEntity s" + where, Long.class);
        var datos = em.createQuery(
                "SELECT s FROM SesionClaseEntity s" + where + " ORDER BY s.semestre, s.dia, s.hora",
                SesionClaseEntity.class);
        if (semestre != null) {
            conteo.setParameter("s", semestre);
            datos.setParameter("s", semestre);
        }
        long total = conteo.getSingleResult();
        List<SesionClase> contenido = datos.setFirstResult(paginacion.offset())
                .setMaxResults(paginacion.tamano())
                .getResultList().stream().map(SesionClaseJpaMapper::aDominio).collect(Collectors.toList());
        return new Pagina<>(contenido, paginacion.pagina(), paginacion.tamano(), total);
    }

    @Override
    public List<SesionClase> listarPorProfesor(Long idProfesor) {
        return em.createQuery(
                        "SELECT s FROM SesionClaseEntity s WHERE s.idProfesor = :id ORDER BY s.dia, s.hora",
                        SesionClaseEntity.class)
                .setParameter("id", idProfesor)
                .getResultList().stream().map(SesionClaseJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public List<SesionClase> listarPorSemestreYJornada(Integer semestre, String jornada) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM SesionClaseEntity s WHERE 1 = 1");
        if (semestre != null) {
            jpql.append(" AND s.semestre = :s");
        }
        if (jornada != null && !jornada.isBlank()) {
            jpql.append(" AND s.jornada = :j");
        }
        jpql.append(" ORDER BY s.dia, s.hora");
        var query = em.createQuery(jpql.toString(), SesionClaseEntity.class);
        if (semestre != null) {
            query.setParameter("s", semestre);
        }
        if (jornada != null && !jornada.isBlank()) {
            query.setParameter("j", jornada);
        }
        return query.getResultList().stream().map(SesionClaseJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public List<SesionClase> listarPorDia(DiaSemana dia) {
        return em.createQuery(
                        "SELECT s FROM SesionClaseEntity s WHERE s.dia = :d ORDER BY s.hora",
                        SesionClaseEntity.class)
                .setParameter("d", dia.getCodigo())
                .getResultList().stream().map(SesionClaseJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public List<SesionClase> listarTodas() {
        return em.createQuery(
                        "SELECT s FROM SesionClaseEntity s ORDER BY s.semestre, s.dia, s.hora",
                        SesionClaseEntity.class)
                .getResultList().stream().map(SesionClaseJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        SesionClaseEntity entidad = em.find(SesionClaseEntity.class, id);
        if (entidad != null) {
            em.remove(entidad);
        }
    }
}
