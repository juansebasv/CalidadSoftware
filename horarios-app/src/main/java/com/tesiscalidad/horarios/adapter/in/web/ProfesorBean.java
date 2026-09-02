package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.CrearProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** Controlador de la vista de gestion de profesores. */
@Named("profesorBean")
@ViewScoped
public class ProfesorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarProfesoresUseCase useCase;

    @Inject
    SesionUsuarioBean sesion;

    private PaginaLazyModel<ProfesorVista> modelo;
    private String filtroTexto;

    private Long id;
    private String codigo;
    private String nombre;
    private String tipoContrato = "PLANTA";
    private boolean disponible = true;
    private Long idUsuario;
    private boolean edicion;

    @PostConstruct
    public void init() {
        this.modelo = new PaginaLazyModel<>(
                (pagina, tamano, texto) -> useCase.listar(texto, pagina, tamano));
    }

    public void aplicarFiltro() {
        modelo.setTexto(filtroTexto);
    }

    public void nuevo() {
        id = null;
        codigo = null;
        nombre = null;
        tipoContrato = "PLANTA";
        disponible = true;
        idUsuario = null;
        edicion = false;
    }

    public void editar(ProfesorVista p) {
        id = p.getId();
        codigo = p.getCodigo();
        nombre = p.getNombre();
        tipoContrato = p.getTipoContrato();
        disponible = p.isDisponible();
        idUsuario = p.getIdUsuario();
        edicion = true;
    }

    public void guardar() {
        try {
            if (edicion) {
                useCase.actualizar(id, new ActualizarProfesorComando(nombre, tipoContrato, disponible, idUsuario),
                        sesion.contexto());
                Mensajes.exito("Profesor actualizado", nombre);
            } else {
                useCase.crear(new CrearProfesorComando(codigo, nombre, tipoContrato, disponible, idUsuario),
                        sesion.contexto());
                Mensajes.exito("Profesor creado", codigo + " - " + nombre);
            }
        } catch (DominioException ex) {
            Mensajes.error("No se pudo guardar", ex.getMessage());
        }
    }

    public void cambiarEstado(ProfesorVista p) {
        try {
            useCase.cambiarEstado(p.getId(), !p.isActivo(), sesion.contexto());
            Mensajes.exito("Estado actualizado", p.getCodigo());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo cambiar el estado", ex.getMessage());
        }
    }

    public void eliminar(ProfesorVista p) {
        try {
            useCase.eliminar(p.getId(), sesion.contexto());
            Mensajes.exito("Profesor eliminado", p.getCodigo());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo eliminar", ex.getMessage());
        }
    }

    public PaginaLazyModel<ProfesorVista> getModelo() {
        return modelo;
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isEdicion() {
        return edicion;
    }
}
