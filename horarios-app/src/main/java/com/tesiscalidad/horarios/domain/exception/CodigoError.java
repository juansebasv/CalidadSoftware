package com.tesiscalidad.horarios.domain.exception;

/**
 * Catalogo unico de codigos de error del dominio. Evita cadenas magicas
 * dispersas y da a la capa de presentacion / API un identificador estable.
 */
public enum CodigoError {

    VALIDACION("ERR-VAL-001", "Datos de entrada no validos"),
    RECURSO_NO_ENCONTRADO("ERR-RES-001", "El recurso solicitado no existe"),
    CONFLICTO_DUPLICADO("ERR-CNF-001", "Ya existe un registro con esos datos"),
    REGLA_NEGOCIO("ERR-BUS-001", "La operacion viola una regla de negocio"),
    CREDENCIALES_INVALIDAS("ERR-SEC-001", "Usuario o contrasena incorrectos"),
    CUENTA_BLOQUEADA("ERR-SEC-002", "La cuenta esta bloqueada temporalmente"),
    CUENTA_INACTIVA("ERR-SEC-003", "La cuenta esta inactiva"),
    NO_AUTORIZADO("ERR-SEC-004", "No tiene permisos para esta operacion"),
    CONFLICTO_HORARIO("ERR-HOR-001", "Existe un cruce de horario"),
    PROFESOR_SIN_PERFIL("ERR-HOR-002", "El profesor no esta habilitado para esa materia"),
    PROFESOR_NO_DISPONIBLE("ERR-HOR-003", "El profesor no tiene disponibilidad en esa franja"),
    INTERNO("ERR-INT-001", "Error interno no esperado");

    private final String codigo;
    private final String mensajePorDefecto;

    CodigoError(String codigo, String mensajePorDefecto) {
        this.codigo = codigo;
        this.mensajePorDefecto = mensajePorDefecto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensajePorDefecto() {
        return mensajePorDefecto;
    }
}
