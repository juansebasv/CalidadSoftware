package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.model.Perfil;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.PerfilEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

/** Adaptador de persistencia de perfiles (habilitacion profesor-materia). */
@ApplicationScoped
public class PerfilRepositoryJpaAdapter implements PerfilRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public List<Perfil> listarPorProfesor(Long idProfesor) {
        return em.createQuery(
                        "SELECT p FROM PerfilEntity p WHERE p.idProfesor = :id", PerfilEntity.class)
                .setParameter("id", idProfesor)
                .getResultList().stream().map(this::aDominio).collect(Collectors.toList());
    }

    @Override
    public List<Perfil> listarPorMateria(Long idMateria) {
        return em.createQuery(
                        "SELECT p FROM PerfilEntity p WHERE p.idMateria = :id", PerfilEntity.class)
                .setParameter("id", idMateria)
                .getResultList().stream().map(this::aDominio).collect(Collectors.toList());
    }

    @Override
    public boolean existe(Long idProfesor, Long idMateria) {
        return em.createQuery(
                        "SELECT COUNT(p) FROM PerfilEntity p WHERE p.idProfesor = :pr AND p.idMateria = :ma",
                        Long.class)
                .setParameter("pr", idProfesor).setParameter("ma", idMateria)
                .getSingleResult() > 0;
    }

    @Override
    public Perfil guardar(Perfil perfil) {
        PerfilEntity entidad = new PerfilEntity();
        entidad.setIdProfesor(perfil.getIdProfesor());
        entidad.setIdMateria(perfil.getIdMateria());
        em.persist(entidad);
        em.flush();
        return aDominio(entidad);
    }

    @Override
    public void eliminar(Long idProfesor, Long idMateria) {
        em.createQuery("DELETE FROM PerfilEntity p WHERE p.idProfesor = :pr AND p.idMateria = :ma")
                .setParameter("pr", idProfesor).setParameter("ma", idMateria)
                .executeUpdate();
    }

    @Override
    public long contarPorMateria(Long idMateria) {
        return em.createQuery(
                        "SELECT COUNT(p) FROM PerfilEntity p WHERE p.idMateria = :id", Long.class)
                .setParameter("id", idMateria).getSingleResult();
    }

    private Perfil aDominio(PerfilEntity e) {
        return Perfil.reconstruir(e.getId(), e.getIdProfesor(), e.getIdMateria());
    }
}
