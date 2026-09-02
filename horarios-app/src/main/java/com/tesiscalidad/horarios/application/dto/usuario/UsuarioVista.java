package com.tesiscalidad.horarios.application.dto.usuario;

import com.tesiscalidad.horarios.domain.model.Usuario;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Proyeccion de lectura de una cuenta. NUNCA expone el hash de la clave. */
public final class UsuarioVista {

    private final Long id;
    private final String login;
    private final String nombre;
    private final String rol;
    private final boolean activo;
    private final boolean bloqueado;
    private final int intentosFallidos;
    private final Instant ultimoAcceso;

    public UsuarioVista(Long id, String login, String nombre, String rol, boolean activo,
                        boolean bloqueado, int intentosFallidos, Instant ultimoAcceso) {
        this.id = id;
        this.login = login;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
        this.bloqueado = bloqueado;
        this.intentosFallidos = intentosFallidos;
        this.ultimoAcceso = ultimoAcceso;
    }

    public static UsuarioVista de(Usuario u, Instant ahora) {
        return new UsuarioVista(u.getId(), u.getLogin(), u.getNombre(), u.getRol().getCodigo(),
                u.isActivo(), u.estaBloqueado(ahora), u.getIntentosFallidos(), u.getUltimoAcceso());
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

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Instant getUltimoAcceso() {
        return ultimoAcceso;
    }

    /** Fecha lista para mostrar en la UI (zona America/Bogota). */
    public String getUltimoAccesoTexto() {
        if (ultimoAcceso == null) {
            return "-";
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.of("America/Bogota")).format(ultimoAcceso);
    }
}
