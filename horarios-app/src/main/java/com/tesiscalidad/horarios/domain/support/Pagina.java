package com.tesiscalidad.horarios.domain.support;

import java.util.List;
import java.util.function.Function;

/** Porcion de resultados + metadatos de paginacion. Inmutable. */
public final class Pagina<T> {

    private final List<T> contenido;
    private final int pagina;
    private final int tamano;
    private final long totalElementos;

    public Pagina(List<T> contenido, int pagina, int tamano, long totalElementos) {
        this.contenido = List.copyOf(contenido);
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
    }

    public List<T> getContenido() {
        return contenido;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTamano() {
        return tamano;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public int getTotalPaginas() {
        return tamano == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
    }

    public boolean isPrimera() {
        return pagina == 0;
    }

    public boolean isUltima() {
        return pagina >= getTotalPaginas() - 1;
    }

    public <R> Pagina<R> mapear(Function<? super T, ? extends R> mapeador) {
        return new Pagina<>(contenido.stream().map(mapeador).collect(java.util.stream.Collectors.toList()),
                pagina, tamano, totalElementos);
    }
}
