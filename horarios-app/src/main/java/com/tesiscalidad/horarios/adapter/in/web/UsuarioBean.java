package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.UsuarioVista;
import com.tesiscalidad.horarios.application.port.in.GestionarUsuariosUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** Controlador de la vista de administracion de cuentas (solo ADMIN). */
@Named("usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    GestionarUsuariosUseCase useCase;

    @Inject
    SesionUsuarioBean sesion;

    private PaginaLazyModel<UsuarioVista> modelo;
    private String filtroTexto;

    private Long id;
    private String login;
    private String nombre;
    private String rol = "CONSULTA";
    private String contrasena;
    private boolean edicion;

    private Long idContrasena;
    private String nuevaContrasena;

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
        login = null;
        nombre = null;
        rol = "CONSULTA";
        contrasena = null;
        edicion = false;
    }

    public void editar(UsuarioVista u) {
        id = u.getId();
        login = u.getLogin();
        nombre = u.getNombre();
        rol = u.getRol();
        contrasena = null;
        edicion = true;
    }

    public void guardar() {
        try {
            if (edicion) {
                useCase.actualizar(id, new ActualizarUsuarioComando(nombre, rol), sesion.contexto());
                Mensajes.exito("Cuenta actualizada", login);
            } else {
                useCase.crear(new CrearUsuarioComando(login, nombre,
                        contrasena == null ? new char[0] : contrasena.toCharArray(), rol), sesion.contexto());
                Mensajes.exito("Cuenta creada", login);
            }
        } catch (DominioException ex) {
            Mensajes.error("No se pudo guardar", ex.getMessage());
        } finally {
            contrasena = null;
        }
    }

    public void prepararContrasena(UsuarioVista u) {
        this.idContrasena = u.getId();
        this.nuevaContrasena = null;
    }

    public void cambiarContrasena() {
        try {
            useCase.cambiarContrasena(idContrasena, new CambiarContrasenaComando(
                    nuevaContrasena == null ? new char[0] : nuevaContrasena.toCharArray()), sesion.contexto());
            Mensajes.exito("Contrasena actualizada", "");
        } catch (DominioException ex) {
            Mensajes.error("No se pudo cambiar la contrasena", ex.getMessage());
        } finally {
            nuevaContrasena = null;
        }
    }

    public void cambiarEstado(UsuarioVista u) {
        try {
            useCase.cambiarEstado(u.getId(), !u.isActivo(), sesion.contexto());
            Mensajes.exito("Estado actualizado", u.getLogin());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo cambiar el estado", ex.getMessage());
        }
    }

    public void desbloquear(UsuarioVista u) {
        try {
            useCase.desbloquear(u.getId(), sesion.contexto());
            Mensajes.exito("Cuenta desbloqueada", u.getLogin());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo desbloquear", ex.getMessage());
        }
    }

    public void eliminar(UsuarioVista u) {
        try {
            useCase.eliminar(u.getId(), sesion.contexto());
            Mensajes.exito("Cuenta eliminada", u.getLogin());
        } catch (DominioException ex) {
            Mensajes.error("No se pudo eliminar", ex.getMessage());
        }
    }

    public PaginaLazyModel<UsuarioVista> getModelo() {
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isEdicion() {
        return edicion;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}
