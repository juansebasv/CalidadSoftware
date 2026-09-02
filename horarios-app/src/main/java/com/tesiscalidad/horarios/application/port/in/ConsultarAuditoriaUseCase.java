package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auditoria.AuditoriaVista;
import com.tesiscalidad.horarios.domain.support.Pagina;

/** Puerto de entrada: consulta de la traza de auditoria (solo gestores). */
public interface ConsultarAuditoriaUseCase {

    Pagina<AuditoriaVista> listar(String usuarioLogin, String accion, int pagina, int tamano,
                                  ContextoPeticion contexto);
}
