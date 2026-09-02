package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RamasAdicionalesTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");

    @Mock ProfesorRepositoryPort profesorRepositorio;
    @Mock UsuarioRepositoryPort usuarioRepositorio;
    @Mock TrazaAuditoria auditoria;

    private ProfesorService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new ProfesorService(profesorRepositorio, usuarioRepositorio, new ParametrosFalsos(),
                new Autorizador(), auditoria);
        lenient().when(profesorRepositorio.guardar(any())).thenAnswer(i -> {
            Profesor p = i.getArgument(0);
            return p.getId() != null ? p
                    : Profesor.reconstruir(7L, p.getCodigo().valor(), p.getNombre(), p.getTipoContrato(),
                    p.isDisponible(), p.getIdUsuario(), p.isActivo());
        });
    }

    @Test
    void cambiarEstadoActivaYDesactiva() {
        Profesor p = Profesor.reconstruir(7L, "DOC-7", "N", TipoContrato.PLANTA, true, null, true);
        when(profesorRepositorio.buscarPorId(7L)).thenReturn(Optional.of(p));
        service.cambiarEstado(7L, false, ctx);
        assertThat(p.isActivo()).isFalse();
        service.cambiarEstado(7L, true, ctx);
        assertThat(p.isActivo()).isTrue();
    }

    @Test
    void actualizarConMismoUsuarioYaVinculadoNoFalla() {
        Profesor existente = Profesor.reconstruir(7L, "DOC-7", "Viejo", TipoContrato.PLANTA, true, 5L, true);
        when(profesorRepositorio.buscarPorId(7L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.buscarPorId(5L)).thenReturn(Optional.of(
                Usuario.reconstruir(5L, "u", "U", HASH, RolUsuario.PROFESOR, true, 0, null, null)));
        when(profesorRepositorio.buscarPorIdUsuario(5L)).thenReturn(Optional.of(existente)); // el mismo

        var v = service.actualizar(7L, new ActualizarProfesorComando("Nuevo", "PLANTA", true, 5L), ctx);
        assertThat(v.getIdUsuario()).isEqualTo(5L);
    }

    @Test
    void listarUsaTamanoExplicitoODefault() {
        Profesor p = Profesor.reconstruir(7L, "DOC-7", "N", TipoContrato.PLANTA, true, null, true);
        when(profesorRepositorio.listar(any(), any())).thenReturn(new Pagina<>(List.of(p), 0, 5, 1));
        assertThat(service.listar("x", 0, 5).getTamano()).isEqualTo(5);   // tamano > 0
        assertThat(service.listar(null, 0, 0).getContenido()).hasSize(1); // tamano <= 0 -> default
    }
}
