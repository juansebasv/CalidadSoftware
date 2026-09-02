package com.tesiscalidad.horarios.application.dto.profesor;

import com.tesiscalidad.horarios.domain.model.Profesor;

/** Proyeccion de lectura de un profesor. */
public final class ProfesorVista {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String tipoContrato;
    private final String tipoContratoEtiqueta;
    private final boolean disponible;
    private final Long idUsuario;
    private final boolean activo;

    public ProfesorVista(Long id, String codigo, String nombre, String tipoContrato,
                         String tipoContratoEtiqueta, boolean disponible, Long idUsuario, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoContrato = tipoContrato;
        this.tipoContratoEtiqueta = tipoContratoEtiqueta;
        this.disponible = disponible;
        this.idUsuario = idUsuario;
        this.activo = activo;
    }

    public static ProfesorVista de(Profesor p) {
        return new ProfesorVista(p.getId(), p.getCodigo().valor(), p.getNombre(),
                p.getTipoContrato().getCodigo(), p.getTipoContrato().getEtiqueta(),
                p.isDisponible(), p.getIdUsuario(), p.isActivo());
    }

    public Long getId() {
        return id;
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

    public String getTipoContratoEtiqueta() {
        return tipoContratoEtiqueta;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public boolean isActivo() {
        return activo;
    }
}
