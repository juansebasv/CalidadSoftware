package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;

/** Puerto de entrada: autenticacion y cierre de sesion. */
public interface AutenticacionUseCase {

    /**
     * Verifica las credenciales y devuelve la identidad autenticada.
     * El arreglo {@code contrasena} se sobrescribe antes de retornar.
     *
     * @throws com.tesiscalidad.horarios.domain.exception.AutenticacionException si el login falla
     */
    SesionAutenticada iniciarSesion(String login, char[] contrasena, ContextoPeticion contexto);

    void cerrarSesion(String login, ContextoPeticion contexto);
}
