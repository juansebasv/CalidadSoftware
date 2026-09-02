package com.tesiscalidad.horarios.domain.enums;

import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import java.util.Arrays;

/**
 * Rol funcional de una cuenta. El {@link #codigo} es el valor persistido en
 * la columna {@code usuario.tipo} (restringido por CHECK en la BD).
 */
public enum RolUsuario {

    ADMIN("ADMIN", true, true),
    COORDINADOR("COORDINADOR", true, false),
    PROFESOR("PROFESOR", false, false),
    CONSULTA("CONSULTA", false, false);

    private final String codigo;
    private final boolean puedeGestionar;
    private final boolean puedeAdministrarUsuarios;

    RolUsuario(String codigo, boolean puedeGestionar, boolean puedeAdministrarUsuarios) {
        this.codigo = codigo;
        this.puedeGestionar = puedeGestionar;
        this.puedeAdministrarUsuarios = puedeAdministrarUsuarios;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Puede crear/editar/eliminar materias, profesores, perfiles y horarios. */
    public boolean puedeGestionar() {
        return puedeGestionar;
    }

    /** Puede administrar cuentas de usuario. */
    public boolean puedeAdministrarUsuarios() {
        return puedeAdministrarUsuarios;
    }

    public static RolUsuario desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(r -> r.codigo.equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Rol de usuario no valido: " + codigo));
    }
}
