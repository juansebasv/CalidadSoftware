package com.tesiscalidad.horarios.domain.enums;

/** Acciones trazadas en la tabla {@code auditoria} (restringidas por CHECK). */
public enum AccionAuditoria {

    LOGIN,
    LOGOUT,
    LOGIN_FALLIDO,
    CUENTA_BLOQUEADA,
    CREAR,
    ACTUALIZAR,
    ELIMINAR,
    CONSULTAR
}
