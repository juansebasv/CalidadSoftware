package com.tesiscalidad.horarios.domain.service;

import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.vo.Franja;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio puro (sin dependencias de infraestructura) que detecta
 * cruces de horario dentro de un conjunto de sesiones:
 * <ul>
 *   <li>un mismo docente con dos sesiones solapadas;</li>
 *   <li>una misma cohorte (semestre + jornada + grupo) con dos sesiones solapadas.</li>
 * </ul>
 * Coste O(n^2) sobre el subconjunto ya filtrado por dia: suficiente para el
 * volumen de un plan de estudios; el filtrado grueso se hace en la BD por indice.
 */
public class DetectorConflictosHorario {

    public List<ConflictoHorario> detectar(List<SesionClase> sesiones) {
        List<ConflictoHorario> conflictos = new ArrayList<>();
        if (sesiones == null || sesiones.size() < 2) {
            return conflictos;
        }
        for (int i = 0; i < sesiones.size(); i++) {
            for (int j = i + 1; j < sesiones.size(); j++) {
                evaluarPar(sesiones.get(i), sesiones.get(j), conflictos);
            }
        }
        return conflictos;
    }

    private void evaluarPar(SesionClase a, SesionClase b, List<ConflictoHorario> acumulador) {
        Franja fa = a.franja();
        Franja fb = b.franja();
        if (!fa.seSolapaCon(fb)) {
            return;
        }
        if (mismoDocente(a, b)) {
            acumulador.add(new ConflictoHorario(ConflictoHorario.Tipo.DOCENTE_SOLAPADO,
                    a.getId(), b.getId(),
                    "Docente " + a.getIdProfesor() + " en " + fa + " y " + fb));
        }
        if (mismaCohorte(a, b)) {
            acumulador.add(new ConflictoHorario(ConflictoHorario.Tipo.COHORTE_SOLAPADA,
                    a.getId(), b.getId(),
                    "Semestre " + a.getSemestre() + " grupo " + a.getGrupo()
                            + " (" + a.getJornada().getEtiqueta() + ") en " + fa + " y " + fb));
        }
    }

    private boolean mismoDocente(SesionClase a, SesionClase b) {
        return a.getIdProfesor() != null && a.getIdProfesor().equals(b.getIdProfesor());
    }

    private boolean mismaCohorte(SesionClase a, SesionClase b) {
        return a.getSemestre() == b.getSemestre()
                && a.getJornada() == b.getJornada()
                && a.getGrupo().equals(b.getGrupo());
    }
}
