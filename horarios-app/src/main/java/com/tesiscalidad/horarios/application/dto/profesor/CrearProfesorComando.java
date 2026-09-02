package com.tesiscalidad.horarios.application.dto.profesor;

/** Datos de entrada para dar de alta un profesor. */
public final class CrearProfesorComando {

    private final String codigo;
    private final String nombre;
    private final String tipoContrato;
    private final boolean disponible;
    private final Long idUsuario;

    public CrearProfesorComando(String codigo, String nombre, String tipoContrato,
                                boolean disponible, Long idUsuario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoContrato = tipoContrato;
        this.disponible = disponible;
        this.idUsuario = idUsuario;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
}
