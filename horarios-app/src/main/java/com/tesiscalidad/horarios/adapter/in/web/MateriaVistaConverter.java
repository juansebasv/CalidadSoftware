package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarMateriasUseCase;
import com.tesiscalidad.horarios.domain.exception.DominioException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

/** Convierte {@link MateriaVista} &lt;-&gt; id para el pickList de perfiles. */
@FacesConverter(value = "materiaVistaConverter", managed = true)
public class MateriaVistaConverter implements Converter<MateriaVista> {

    @Inject
    GestionarMateriasUseCase useCase;

    @Override
    public String getAsString(FacesContext context, UIComponent component, MateriaVista valor) {
        return valor == null || valor.getId() == null ? "" : String.valueOf(valor.getId());
    }

    @Override
    public MateriaVista getAsObject(FacesContext context, UIComponent component, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return useCase.obtener(Long.valueOf(valor.trim()));
        } catch (NumberFormatException | DominioException ex) {
            return null;
        }
    }
}
