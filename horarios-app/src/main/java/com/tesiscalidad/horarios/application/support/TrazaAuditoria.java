package com.tesiscalidad.horarios.application.support;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.port.out.AuditoriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.port.out.RelojPort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Fachada de escritura de auditoria reutilizada por todos los casos de uso.
 * Centraliza el formato del registro y respeta el flag de configuracion.
 */
@ApplicationScoped
public class TrazaAuditoria {

    private final AuditoriaRepositoryPort repositorio;
    private final RelojPort reloj;
    private final ParametrosAplicacionPort parametros;

    protected TrazaAuditoria() {
        this(null, null, null);
    }

    @Inject
    public TrazaAuditoria(AuditoriaRepositoryPort repositorio, RelojPort reloj,
                          ParametrosAplicacionPort parametros) {
        this.repositorio = repositorio;
        this.reloj = reloj;
        this.parametros = parametros;
    }

    public void exito(AccionAuditoria accion, String entidad, Object entidadId,
                      String detalle, ContextoPeticion contexto) {
        registrar(accion, ResultadoAuditoria.EXITO, entidad, entidadId, detalle, contexto);
    }

    public void error(AccionAuditoria accion, String entidad, Object entidadId,
                      String detalle, ContextoPeticion contexto) {
        registrar(accion, ResultadoAuditoria.ERROR, entidad, entidadId, detalle, contexto);
    }

    private void registrar(AccionAuditoria accion, ResultadoAuditoria resultado, String entidad,
                           Object entidadId, String detalle, ContextoPeticion contexto) {
        if (!parametros.auditoriaHabilitada()) {
            return;
        }
        RegistroAuditoria registro = RegistroAuditoria.builder(accion, resultado)
                .fecha(reloj.ahora())
                .usuario(contexto == null ? null : contexto.getUsuarioLogin())
                .entidad(entidad, entidadId)
                .detalle(detalle)
                .ip(contexto == null ? null : contexto.getIp())
                .correlationId(contexto == null ? null : contexto.getCorrelationId())
                .build();
        repositorio.registrar(registro);
    }
}
