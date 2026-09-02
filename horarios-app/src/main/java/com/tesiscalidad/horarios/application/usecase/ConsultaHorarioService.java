package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.horario.ConflictoVista;
import com.tesiscalidad.horarios.application.dto.horario.ConteoVista;
import com.tesiscalidad.horarios.application.dto.horario.TableroVista;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarHorarioUseCase;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.service.DetectorConflictosHorario;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Consultas de horario y agregados del tablero. Solo lectura. */
@ApplicationScoped
public class ConsultaHorarioService implements ConsultarHorarioUseCase {

    private static final int MUESTRA_CONTEO = 1;
    private static final int TOPE_CONTEO = 1;

    private final SesionClaseRepositoryPort sesionRepositorio;
    private final MateriaRepositoryPort materiaRepositorio;
    private final ProfesorRepositoryPort profesorRepositorio;
    private final UsuarioRepositoryPort usuarioRepositorio;
    private final DetectorConflictosHorario detector;

    protected ConsultaHorarioService() {
        this(null, null, null, null, null);
    }

    @Inject
    public ConsultaHorarioService(SesionClaseRepositoryPort sesionRepositorio,
                                  MateriaRepositoryPort materiaRepositorio,
                                  ProfesorRepositoryPort profesorRepositorio,
                                  UsuarioRepositoryPort usuarioRepositorio,
                                  DetectorConflictosHorario detector) {
        this.sesionRepositorio = sesionRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.detector = detector;
    }

    @Override
    public List<SesionClaseVista> horarioDeProfesor(Long idProfesor) {
        return proyectar(sesionRepositorio.listarPorProfesor(idProfesor));
    }

    @Override
    public List<SesionClaseVista> horarioPorSemestre(int semestre, String jornada) {
        return proyectar(sesionRepositorio.listarPorSemestreYJornada(semestre, jornada));
    }

    @Override
    public List<ConflictoVista> detectarConflictos() {
        return detector.detectar(sesionRepositorio.listarTodas()).stream()
                .map(ConflictoVista::de)
                .collect(Collectors.toList());
    }

    @Override
    public TableroVista tablero() {
        List<SesionClase> sesiones = sesionRepositorio.listarTodas();
        List<Materia> materias = materiaRepositorio.listarActivas();
        List<Profesor> profesores = profesorRepositorio.listarActivos();

        long sinDocente = sesiones.stream().filter(s -> !s.tieneProfesor()).count();
        long conflictos = detector.detectar(sesiones).size();

        return new TableroVista(
                materiaRepositorio.listar(null, null, Paginacion.de(0, MUESTRA_CONTEO, TOPE_CONTEO)).getTotalElementos(),
                profesorRepositorio.listar(null, Paginacion.de(0, MUESTRA_CONTEO, TOPE_CONTEO)).getTotalElementos(),
                usuarioRepositorio.listar(null, Paginacion.de(0, MUESTRA_CONTEO, TOPE_CONTEO)).getTotalElementos(),
                sesiones.size(), sinDocente, conflictos,
                conteoPorDia(sesiones),
                conteoPor(sesiones, s -> "Semestre " + s.getSemestre()),
                conteoPor(materias, m -> m.getTipo().getEtiqueta()),
                conteoPor(profesores, p -> p.getTipoContrato().getEtiqueta()));
    }

    private List<ConteoVista> conteoPorDia(List<SesionClase> sesiones) {
        List<ConteoVista> resultado = new ArrayList<>();
        for (DiaSemana dia : DiaSemana.values()) {
            long total = sesiones.stream().filter(s -> s.getDia() == dia).count();
            if (total > 0) {
                resultado.add(new ConteoVista(dia.getEtiqueta(), total));
            }
        }
        return resultado;
    }

    private <T> List<ConteoVista> conteoPor(List<T> elementos, java.util.function.Function<T, String> clave) {
        Map<String, Long> agrupado = elementos.stream()
                .collect(Collectors.groupingBy(clave, LinkedHashMap::new, Collectors.counting()));
        return agrupado.entrySet().stream()
                .map(e -> new ConteoVista(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<SesionClaseVista> proyectar(List<SesionClase> sesiones) {
        Map<Long, Materia> materias = materiaRepositorio.listarActivas().stream()
                .collect(Collectors.toMap(Materia::getId, m -> m, (a, b) -> a));
        Map<Long, Profesor> profesores = profesorRepositorio.listarActivos().stream()
                .collect(Collectors.toMap(Profesor::getId, p -> p, (a, b) -> a));
        List<SesionClaseVista> vistas = new ArrayList<>(sesiones.size());
        for (SesionClase s : sesiones) {
            Materia m = materias.get(s.getIdMateria());
            Profesor p = s.getIdProfesor() == null ? null : profesores.get(s.getIdProfesor());
            vistas.add(SesionClaseVista.de(s,
                    m == null ? "?" : m.getCodigo().valor(),
                    m == null ? "?" : m.getNombre(),
                    p == null ? "(sin asignar)" : p.getNombre()));
        }
        return vistas;
    }
}
