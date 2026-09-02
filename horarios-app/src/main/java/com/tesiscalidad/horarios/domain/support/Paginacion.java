package com.tesiscalidad.horarios.domain.support;

/**
 * Parametros de paginacion para lecturas de gran volumen. {@code pagina} es
 * 0-based. El tamano se acota para impedir consultas sin limite.
 */
public final class Paginacion {

    private final int pagina;
    private final int tamano;

    private Paginacion(int pagina, int tamano) {
        this.pagina = pagina;
        this.tamano = tamano;
    }

    public static Paginacion de(int pagina, int tamano, int tamanoMaximo) {
        int p = Math.max(0, pagina);
        int t = tamano <= 0 ? 1 : Math.min(tamano, Math.max(1, tamanoMaximo));
        return new Paginacion(p, t);
    }

    public int pagina() {
        return pagina;
    }

    public int tamano() {
        return tamano;
    }

    public int offset() {
        return pagina * tamano;
    }
}
