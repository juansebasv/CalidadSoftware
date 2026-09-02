package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.vo.ClaveHash;

/**
 * Puerto de hashing de contrasenas. La implementacion usa un algoritmo
 * adaptativo resistente a fuerza bruta (BCrypt) con verificacion en tiempo
 * constante.
 */
public interface HasheadorContrasenaPort {

    /** Deriva el hash de una contrasena en claro. No conserva el arreglo. */
    ClaveHash hashear(char[] contrasenaPlano);

    /** Comparacion en tiempo constante entre la contrasena y un hash. */
    boolean verificar(char[] contrasenaPlano, ClaveHash hash);

    /** {@code true} si el hash usa un costo inferior al configurado y conviene rehacerlo. */
    boolean requiereRehash(ClaveHash hash);
}
