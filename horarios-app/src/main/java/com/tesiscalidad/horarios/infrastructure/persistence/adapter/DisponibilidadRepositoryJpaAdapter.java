package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.DisponibilidadProfesorEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.DisponibilidadJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador de persistencia de la disponibilidad docente. */
@ApplicationScoped
public class DisponibilidadRepositoryJpaAdapter implements DisponibilidadRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    @Override
    public List<DisponibilidadProfesor> listarPorProfesor(Long idProfesor) {
        return em.createQuery(
                        "SELECT d FROM DisponibilidadProfesorEntity d WHERE d.idProfesor = :id "
                                + "ORDER BY d.dia, d.hora", DisponibilidadProfesorEntity.class)
                .setParameter("id", idProfesor)
                .getResultList().stream().map(DisponibilidadJpaMapper::aDominio).collect(Collectors.toList());
    }

    @Override
    public Optional<DisponibilidadProfesor> buscar(Long idProfesor, DiaSemana dia, int hora) {
        return em.createQuery(
                        "SELECT d FROM DisponibilidadProfesorEntity d WHERE d.idProfesor = :id "
                                + "AND d.dia = :dia AND d.hora = :hora", DisponibilidadProfesorEntity.class)
                .setParameter("id", idProfesor)
                .setParameter("dia", dia.getCodigo())
                .setParameter("hora", String.format("%02d", hora))
                .getResultStream().findFirst().map(DisponibilidadJpaMapper::aDominio);
    }

    @Override
    public DisponibilidadProfesor guardar(DisponibilidadProfesor disponibilidad) {
        DisponibilidadProfesorEntity entidad;
        if (disponibilidad.getId() == null) {
            entidad = DisponibilidadJpaMapper.aEntidadNueva(disponibilidad);
            em.persist(entidad);
        } else {
            entidad = em.find(DisponibilidadProfesorEntity.class, disponibilidad.getId());
            DisponibilidadJpaMapper.volcar(disponibilidad, entidad);
            entidad = em.merge(entidad);
        }
        em.flush();
        return DisponibilidadJpaMapper.aDominio(entidad);
    }

    @Override
    public void guardarLote(List<DisponibilidadProfesor> disponibilidades) {
        int i = 0;
        for (DisponibilidadProfesor d : disponibilidades) {
            em.persist(DisponibilidadJpaMapper.aEntidadNueva(d));
            if (++i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
    }

    @Override
    public void eliminar(Long id) {
        DisponibilidadProfesorEntity entidad = em.find(DisponibilidadProfesorEntity.class, id);
        if (entidad != null) {
            em.remove(entidad);
        }
    }

    @Override
    public void eliminarPorProfesor(Long idProfesor) {
        em.createQuery("DELETE FROM DisponibilidadProfesorEntity d WHERE d.idProfesor = :id")
                .setParameter("id", idProfesor)
                .executeUpdate();
    }
}
