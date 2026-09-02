package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auditoria.AuditoriaVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarAuditoriaUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.domain.port.out.AuditoriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/** Consulta paginada de la traza de auditoria. */
@ApplicationScoped
public class AuditoriaConsultaService implements ConsultarAuditoriaUseCase {

    private final AuditoriaRepositoryPort repositorio;
    private final ParametrosAplicacionPort parametros;
    private final Autorizador autorizador;

    protected AuditoriaConsultaService() {
        this(null, null, null);
    }

    @Inject
    public AuditoriaConsultaService(AuditoriaRepositoryPort repositorio, ParametrosAplicacionPort parametros,
                                    Autorizador autorizador) {
        this.repositorio = repositorio;
        this.parametros = parametros;
        this.autorizador = autorizador;
    }

    @Override
    public Pagina<AuditoriaVista> listar(String usuarioLogin, String accion, int pagina, int tamano,
                                         ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Paginacion paginacion = Paginacion.de(pagina,
                tamano <= 0 ? parametros.tamanoPaginaPorDefecto() : tamano,
                parametros.tamanoPaginaMaximo());
        return repositorio.listar(vacioANull(usuarioLogin), vacioANull(accion), paginacion)
                .mapear(AuditoriaVista::de);
    }

    private String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
