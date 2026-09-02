package com.tesiscalidad.horarios.infrastructure.persistence.adapter;

import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.port.out.AuditoriaRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.AuditoriaEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.mapper.AuditoriaJpaMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/** Adaptador de persistencia de la traza de auditoria. */
@ApplicationScoped
public class AuditoriaRepositoryJpaAdapter implements AuditoriaRepositoryPort {

    @PersistenceContext(unitName = "horariosPU")
    private EntityManager em;

    /**
     * Se ejecuta en una transaccion nueva para que la traza (sobre todo los
     * LOGIN_FALLIDO / CUENTA_BLOQUEADA) persista aunque la transaccion del caso
     * de uso termine en rollback.
     */
    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrar(RegistroAuditoria registro) {
        em.persist(AuditoriaJpaMapper.aEntidadNueva(registro));
        em.flush();
    }

    @Override
    public Pagina<RegistroAuditoria> listar(String usuarioLogin, String accion, Paginacion paginacion) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (usuarioLogin != null) {
            where.append(" AND LOWER(a.usuarioLogin) = :u");
        }
        if (accion != null) {
            where.append(" AND a.accion = :ac");
        }
        var conteo = em.createQuery("SELECT COUNT(a) FROM AuditoriaEntity a" + where, Long.class);
        var datos = em.createQuery("SELECT a FROM AuditoriaEntity a" + where + " ORDER BY a.fecha DESC",
                AuditoriaEntity.class);
        if (usuarioLogin != null) {
            conteo.setParameter("u", usuarioLogin.toLowerCase());
            datos.setParameter("u", usuarioLogin.toLowerCase());
        }
        if (accion != null) {
            conteo.setParameter("ac", accion);
            datos.setParameter("ac", accion);
        }
        long total = conteo.getSingleResult();
        List<RegistroAuditoria> contenido = datos.setFirstResult(paginacion.offset())
                .setMaxResults(paginacion.tamano())
                .getResultList().stream().map(AuditoriaJpaMapper::aDominio).collect(Collectors.toList());
        return new Pagina<>(contenido, paginacion.pagina(), paginacion.tamano(), total);
    }
}
