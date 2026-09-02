package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

/** Puerto de persistencia de la traza de auditoria. */
public interface AuditoriaRepositoryPort {

    void registrar(RegistroAuditoria registro);

    Pagina<RegistroAuditoria> listar(String usuarioLogin, String accion, Paginacion paginacion);
}
