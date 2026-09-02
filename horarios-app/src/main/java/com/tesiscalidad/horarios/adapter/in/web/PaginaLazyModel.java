package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.domain.support.Pagina;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * Adaptador entre el {@link Pagina} del dominio y el {@link LazyDataModel} de
 * PrimeFaces 8, para tablas que pueden crecer a gran volumen: la BD solo
 * entrega la pagina visible y el conteo total.
 */
public class PaginaLazyModel<T> extends LazyDataModel<T> {

    private static final long serialVersionUID = 1L;

    /** Carga una pagina (0-based) del origen de datos. */
    public interface Cargador<T> {
        Pagina<T> cargar(int pagina, int tamano, String texto);
    }

    private final transient Cargador<T> cargador;
    private String texto;

    public PaginaLazyModel(Cargador<T> cargador) {
        this.cargador = cargador;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                        Map<String, FilterMeta> filterBy) {
        return cargarPagina(first, pageSize);
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy,
                        Map<String, FilterMeta> filterBy) {
        return cargarPagina(first, pageSize);
    }

    private List<T> cargarPagina(int first, int pageSize) {
        int tamano = pageSize <= 0 ? 1 : pageSize;
        int pagina = first / tamano;
        Pagina<T> resultado = cargador.cargar(pagina, tamano, texto);
        setRowCount((int) Math.min(Integer.MAX_VALUE, resultado.getTotalElementos()));
        return resultado.getContenido();
    }
}
