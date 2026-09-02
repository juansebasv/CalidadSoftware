package com.tesiscalidad.horarios.domain.service;

import com.tesiscalidad.horarios.domain.exception.CodigoError;
import com.tesiscalidad.horarios.domain.exception.ReglaNegocioException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.vo.Franja;

import java.util.List;

/**
 * Servicio de dominio puro que valida si una sesion candidata puede programarse:
 * el docente esta habilitado (perfil), tiene disponibilidad que cubre la franja
 * y no se cruza con otras sesiones suyas ni de la misma cohorte.
 */
public class ValidadorProgramacionSesion {

    public void validar(SesionClase candidata,
                        boolean profesorHabilitado,
                        List<DisponibilidadProfesor> disponibilidadDelProfesor,
                        List<SesionClase> sesionesExistentes) {

        if (candidata.tieneProfesor()) {
            exigeHabilitacion(profesorHabilitado);
            exigeDisponibilidad(candidata, disponibilidadDelProfesor);
        }
        exigeSinCruces(candidata, sesionesExistentes);
    }

    private void exigeHabilitacion(boolean profesorHabilitado) {
        if (!profesorHabilitado) {
            throw new ReglaNegocioException(CodigoError.PROFESOR_SIN_PERFIL,
                    CodigoError.PROFESOR_SIN_PERFIL.getMensajePorDefecto());
        }
    }

    private void exigeDisponibilidad(SesionClase candidata, List<DisponibilidadProfesor> disponibilidad) {
        Franja franja = candidata.franja();
        for (int hora = franja.horaInicio(); hora < franja.horaFin(); hora++) {
            final int h = hora;
            boolean cubierta = disponibilidad.stream()
                    .anyMatch(d -> d.getDia() == candidata.getDia() && d.getHora() == h);
            if (!cubierta) {
                throw new ReglaNegocioException(CodigoError.PROFESOR_NO_DISPONIBLE,
                        "El profesor no tiene disponibilidad a las " + h + ":00 del "
                                + candidata.getDia().getEtiqueta() + ".");
            }
        }
    }

    private void exigeSinCruces(SesionClase candidata, List<SesionClase> existentes) {
        Franja fc = candidata.franja();
        for (SesionClase otra : existentes) {
            if (candidata.getId() != null && candidata.getId().equals(otra.getId())) {
                continue;
            }
            if (!fc.seSolapaCon(otra.franja())) {
                continue;
            }
            if (candidata.tieneProfesor() && candidata.getIdProfesor().equals(otra.getIdProfesor())) {
                throw new ReglaNegocioException(CodigoError.CONFLICTO_HORARIO,
                        "El docente ya dicta otra sesion que se cruza en " + otra.franja() + ".");
            }
            if (mismaCohorte(candidata, otra)) {
                throw new ReglaNegocioException(CodigoError.CONFLICTO_HORARIO,
                        "El grupo " + candidata.getGrupo() + " (semestre " + candidata.getSemestre()
                                + ") ya tiene otra sesion que se cruza en " + otra.franja() + ".");
            }
        }
    }

    private boolean mismaCohorte(SesionClase a, SesionClase b) {
        return a.getSemestre() == b.getSemestre()
                && a.getJornada() == b.getJornada()
                && a.getGrupo().equals(b.getGrupo());
    }
}
