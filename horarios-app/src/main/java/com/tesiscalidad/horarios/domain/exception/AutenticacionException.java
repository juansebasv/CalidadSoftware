package com.tesiscalidad.horarios.domain.exception;

/**
 * Fallo de autenticacion. El mensaje es deliberadamente generico para no
 * revelar si fallo el usuario o la contrasena (enumeracion de cuentas).
 */
public class AutenticacionException extends DominioException {

    private static final long serialVersionUID = 1L;

    public AutenticacionException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static AutenticacionException credencialesInvalidas() {
        return new AutenticacionException(CodigoError.CREDENCIALES_INVALIDAS,
                CodigoError.CREDENCIALES_INVALIDAS.getMensajePorDefecto());
    }

    public static AutenticacionException cuentaBloqueada(long minutosRestantes) {
        return new AutenticacionException(CodigoError.CUENTA_BLOQUEADA,
                "Cuenta bloqueada. Reintente en " + minutosRestantes + " minuto(s).");
    }

    public static AutenticacionException cuentaInactiva() {
        return new AutenticacionException(CodigoError.CUENTA_INACTIVA,
                CodigoError.CUENTA_INACTIVA.getMensajePorDefecto());
    }
}
