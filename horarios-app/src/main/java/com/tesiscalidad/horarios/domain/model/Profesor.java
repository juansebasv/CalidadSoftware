package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.vo.Codigo;

/** Docente. Puede tener (o no) una cuenta de usuario asociada. */
public final class Profesor {

    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 80;

    private final Long id;
    private final Codigo codigo;
    private String nombre;
    private TipoContrato tipoContrato;
    private boolean disponible;
    private Long idUsuario;
    private boolean activo;

    private Profesor(Long id, Codigo codigo, String nombre, TipoContrato tipoContrato,
                     boolean disponible, Long idUsuario, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoContrato = tipoContrato;
        this.disponible = disponible;
        this.idUsuario = idUsuario;
        this.activo = activo;
    }

    public static Profesor crear(String codigo, String nombre, TipoContrato tipoContrato, boolean disponible) {
        return new Profesor(null, Codigo.de(codigo),
                Preconditions.longitud(nombre, "nombre", NOMBRE_MIN, NOMBRE_MAX),
                Preconditions.requerido(tipoContrato, "tipoContrato"),
                disponible, null, true);
    }

    public static Profesor reconstruir(Long id, String codigo, String nombre, TipoContrato tipoContrato,
                                       boolean disponible, Long idUsuario, boolean activo) {
        return new Profesor(Preconditions.requerido(id, "id"), Codigo.de(codigo), nombre, tipoContrato,
                disponible, idUsuario, activo);
    }

    public void actualizar(String nombre, TipoContrato tipoContrato, boolean disponible) {
        this.nombre = Preconditions.longitud(nombre, "nombre", NOMBRE_MIN, NOMBRE_MAX);
        this.tipoContrato = Preconditions.requerido(tipoContrato, "tipoContrato");
        this.disponible = disponible;
    }

    public void vincularUsuario(Long idUsuario) {
        this.idUsuario = Preconditions.requerido(idUsuario, "idUsuario");
    }

    public void desvincularUsuario() {
        this.idUsuario = null;
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

    public Codigo getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
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
