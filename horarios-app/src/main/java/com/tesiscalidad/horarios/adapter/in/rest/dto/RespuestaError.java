package com.tesiscalidad.horarios.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Cuerpo JSON estandar de error de la API. */
@Schema(name = "RespuestaError", description = "Detalle de un error de la API")
public class RespuestaError {

    @Schema(example = "ERR-VAL-001")
    public String codigo;

    @Schema(example = "El campo 'nombre' es obligatorio.")
    public String mensaje;

    @Schema(example = "/api/materias")
    public String ruta;

    public RespuestaError() {
    }

    public RespuestaError(String codigo, String mensaje, String ruta) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }
}
