package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.application.port.in.AutenticacionUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/** Respaldo del formulario de inicio de sesion. */
@Named("loginBean")
@RequestScoped
public class LoginBean {

    private static final Logger LOG = LoggerFactory.getLogger(LoginBean.class);

    @Inject
    AutenticacionUseCase autenticacionUseCase;

    @Inject
    SesionUsuarioBean sesionUsuario;

    private String login;
    private String contrasena;

    /** Navega al tablero si las credenciales son correctas. */
    public String iniciar() {
        char[] clave = contrasena == null ? new char[0] : contrasena.toCharArray();
        try {
            SesionAutenticada sesion = autenticacionUseCase.iniciarSesion(login, clave, sesionUsuario.contexto());
            renovarIdSesion();
            sesionUsuario.establecerSesion(sesion);
            LOG.info("Login correcto para '{}'", sesion.getLogin());
            return "/app/dashboard.xhtml?faces-redirect=true";
        } catch (DominioException ex) {
            Mensajes.error("Acceso denegado", ex.getMessage());
            return null;
        } finally {
            this.contrasena = null;
        }
    }

    /** Prevencion de fijacion de sesion: nuevo id de sesion tras autenticar. */
    private void renovarIdSesion() {
        Object req = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (req instanceof HttpServletRequest) {
            ((HttpServletRequest) req).changeSessionId();
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
