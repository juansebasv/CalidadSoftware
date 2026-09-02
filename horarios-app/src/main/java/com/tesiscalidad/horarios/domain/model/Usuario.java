package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.AutenticacionException;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.domain.vo.PoliticaBloqueo;

import java.time.Duration;
import java.time.Instant;

/**
 * Cuenta de acceso al sistema. Concentra la logica de bloqueo por intentos
 * fallidos y la comprobacion de que la cuenta puede autenticarse.
 */
public final class Usuario {

    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 80;

    private final Long id;
    private final String login;
    private String nombre;
    private ClaveHash clave;
    private RolUsuario rol;
    private boolean activo;
    private int intentosFallidos;
    private Instant bloqueadoHasta;
    private Instant ultimoAcceso;

    private Usuario(Long id, String login, String nombre, ClaveHash clave, RolUsuario rol,
                    boolean activo, int intentosFallidos, Instant bloqueadoHasta, Instant ultimoAcceso) {
        this.id = id;
        this.login = login;
        this.nombre = nombre;
        this.clave = clave;
        this.rol = rol;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.ultimoAcceso = ultimoAcceso;
    }

    /** Alta de una cuenta nueva (sin id todavia). */
    public static Usuario crear(String login, String nombre, ClaveHash clave, RolUsuario rol) {
        String loginNorm = Preconditions
                .coincidePatron(login == null ? null : login.trim().toLowerCase(), "login",
                        "^[A-Za-z0-9._-]{4,50}$");
        return new Usuario(null, loginNorm,
                Preconditions.longitud(nombre, "nombre", NOMBRE_MIN, NOMBRE_MAX),
                Preconditions.requerido(clave, "clave"),
                Preconditions.requerido(rol, "rol"),
                true, 0, null, null);
    }

    /** Reconstruccion desde persistencia. */
    public static Usuario reconstruir(Long id, String login, String nombre, ClaveHash clave, RolUsuario rol,
                                      boolean activo, int intentosFallidos,
                                      Instant bloqueadoHasta, Instant ultimoAcceso) {
        return new Usuario(Preconditions.requerido(id, "id"), Preconditions.textoRequerido(login, "login"),
                nombre, clave, rol, activo, Math.max(0, intentosFallidos), bloqueadoHasta, ultimoAcceso);
    }

    public boolean estaBloqueado(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public long minutosRestantesBloqueo(Instant ahora) {
        if (!estaBloqueado(ahora)) {
            return 0;
        }
        return Math.max(1, Duration.between(ahora, bloqueadoHasta).toMinutes() + 1);
    }

    /** Lanza si la cuenta esta inactiva o bloqueada. No comprueba la contrasena. */
    public void asegurarPuedeAutenticar(Instant ahora) {
        if (!activo) {
            throw AutenticacionException.cuentaInactiva();
        }
        if (estaBloqueado(ahora)) {
            throw AutenticacionException.cuentaBloqueada(minutosRestantesBloqueo(ahora));
        }
    }

    /**
     * Registra un intento de login fallido y, si se alcanza el maximo, bloquea
     * la cuenta temporalmente.
     *
     * @return {@code true} si este intento produjo el bloqueo.
     */
    public boolean registrarIntentoFallido(PoliticaBloqueo politica, Instant ahora) {
        Preconditions.requerido(politica, "politica");
        intentosFallidos++;
        if (intentosFallidos >= politica.maximoIntentos()) {
            bloqueadoHasta = ahora.plus(Duration.ofMinutes(politica.minutosBloqueo()));
            return true;
        }
        return false;
    }

    /** Reinicia contadores tras un login correcto. */
    public void registrarAccesoExitoso(Instant ahora) {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.ultimoAcceso = Preconditions.requerido(ahora, "ahora");
    }

    public void cambiarClave(ClaveHash nueva) {
        this.clave = Preconditions.requerido(nueva, "clave");
    }

    public void cambiarRol(RolUsuario nuevo) {
        this.rol = Preconditions.requerido(nuevo, "rol");
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = Preconditions.longitud(nuevoNombre, "nombre", NOMBRE_MIN, NOMBRE_MAX);
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getNombre() {
        return nombre;
    }

    public ClaveHash getClave() {
        return clave;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public Instant getUltimoAcceso() {
        return ultimoAcceso;
    }
}
