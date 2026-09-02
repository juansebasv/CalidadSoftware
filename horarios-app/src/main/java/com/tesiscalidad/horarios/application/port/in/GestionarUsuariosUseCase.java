package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.UsuarioVista;
import com.tesiscalidad.horarios.domain.support.Pagina;

/** Puerto de entrada: administracion de cuentas de usuario. */
public interface GestionarUsuariosUseCase {

    UsuarioVista crear(CrearUsuarioComando comando, ContextoPeticion contexto);

    UsuarioVista actualizar(Long id, ActualizarUsuarioComando comando, ContextoPeticion contexto);

    void cambiarContrasena(Long id, CambiarContrasenaComando comando, ContextoPeticion contexto);

    void cambiarEstado(Long id, boolean activo, ContextoPeticion contexto);

    void desbloquear(Long id, ContextoPeticion contexto);

    void eliminar(Long id, ContextoPeticion contexto);

    UsuarioVista obtener(Long id);

    Pagina<UsuarioVista> listar(String texto, int pagina, int tamano);
}
