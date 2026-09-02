package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.AuditoriaEntity;

/** Conversion {@link AuditoriaEntity} &lt;-&gt; {@link RegistroAuditoria}. */
public final class AuditoriaJpaMapper {

    private AuditoriaJpaMapper() {
    }

    public static RegistroAuditoria aDominio(AuditoriaEntity e) {
        return RegistroAuditoria.reconstruir(e.getId(), TiempoJpa.aInstant(e.getFecha()),
                e.getUsuarioLogin(), AccionAuditoria.valueOf(e.getAccion()), e.getEntidad(),
                e.getEntidadId(), ResultadoAuditoria.valueOf(e.getResultado()), e.getDetalle(),
                e.getIp(), e.getCorrelationId());
    }

    public static AuditoriaEntity aEntidadNueva(RegistroAuditoria r) {
        AuditoriaEntity e = new AuditoriaEntity();
        e.setFecha(TiempoJpa.aOffset(r.getFecha()));
        e.setUsuarioLogin(r.getUsuarioLogin());
        e.setAccion(r.getAccion().name());
        e.setEntidad(r.getEntidad());
        e.setEntidadId(r.getEntidadId());
        e.setResultado(r.getResultado().name());
        e.setDetalle(r.getDetalle());
        e.setIp(r.getIp());
        e.setCorrelationId(r.getCorrelationId());
        return e;
    }
}
