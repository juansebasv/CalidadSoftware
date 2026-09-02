package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import java.util.Optional;

/** Puerto de persistencia de cuentas de usuario. */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorLogin(String login);

    boolean existeLogin(String login);

    Usuario guardar(Usuario usuario);

    /** Actualiza SOLO los campos de control de acceso (intentos, bloqueo, ultimo acceso). */
    void actualizarEstadoAcceso(Usuario usuario);

    Pagina<Usuario> listar(String texto, Paginacion paginacion);

    void eliminar(Long id);
}
