package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.materia.ActualizarMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarMateriasUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** Controlador de la vista de gestion de materias. */
@Named("materiaBean")
@ViewScoped
public class MateriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarMateriasUseCase useCase;

    @Inject
    SesionUsuarioBean sesion;

    private PaginaLazyModel<MateriaVista> modelo;
    private String filtroTexto;
    private Integer filtroSemestre;

    private Long id;
    private String codigo;
    private String nombre;
    private String tipo = "T";
    private int creditos = 3;
    private int intensidadHoraria = 4;
    private int semestre = 1;
    private boolean edicion;

    @PostConstruct
    public void init() {
        this.modelo = new PaginaLazyModel<>(
                (pagina, tamano, texto) -> useCase.listar(texto, filtroSemestre, pagina, tamano));
    }

    public void aplicarFiltro() {
        modelo.setTexto(filtroTexto);
    }

    public void nuevo() {
        this.id = null;
        this.codigo = null;
        this.nombre = null;
        this.tipo = "T";
        this.creditos = 3;
        this.intensidadHoraria = 4;
        this.semestre = 1;
        this.edicion = false;
    }

    public void editar(MateriaVista m) {
        this.id = m.getId();
        this.codigo = m.getCodigo();
        this.nombre = m.getNombre();
        this.tipo = m.getTipo();
        this.creditos = m.getCreditos();
        this.intensidadHoraria = m.getIntensidadHoraria();
        this.semestre = m.getSemestre();
        this.edicion = true;
    }

    public void guardar() {
        try {
            if (edicion) {
                useCase.actualizar(id, new ActualizarMateriaComando(nombre, tipo, creditos,
                        intensidadHoraria, semestre), sesion.contexto());
                Mensajes.exito("Materia actualizada", nombre);
            } else {
                useCase.crear(new CrearMateriaComando(codigo, nombre, tipo, creditos,
                        intensidadHoraria, semestre), sesion.contexto());
                Mensajes.exito("Materia creada", codigo + " - " + nombre);
            }
        } catch (DominioException ex) {
            Mensajes.error("No se pudo guardar", ex.getMessage());
        }
    }

    public void cambiarEstado(MateriaVista m) {
        try {
            useCase.cambiarEstado(m.getId(), !m.isActiva(), sesion.contexto());
            Mensajes.exito("Estado actualizado", m.getCodigo());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo cambiar el estado", ex.getMessage());
        }
    }

    public void eliminar(MateriaVista m) {
        try {
            useCase.eliminar(m.getId(), sesion.contexto());
            Mensajes.exito("Materia eliminada", m.getCodigo());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo eliminar", ex.getMessage());
        }
    }

    public PaginaLazyModel<MateriaVista> getModelo() {
        return modelo;
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public Integer getFiltroSemestre() {
        return filtroSemestre;
    }

    public void setFiltroSemestre(Integer filtroSemestre) {
        this.filtroSemestre = filtroSemestre;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public int getIntensidadHoraria() {
        return intensidadHoraria;
    }

    public void setIntensidadHoraria(int intensidadHoraria) {
        this.intensidadHoraria = intensidadHoraria;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public boolean isEdicion() {
        return edicion;
    }
}
