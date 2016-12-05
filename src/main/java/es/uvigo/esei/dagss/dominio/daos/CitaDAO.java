/*
 Proyecto Java EE, DAGSS-2014
 */

package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Cita;
import es.uvigo.esei.dagss.dominio.entidades.Medico;
import es.uvigo.esei.dagss.dominio.entidades.Receta;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.List;


@Stateless
@LocalBean
public class CitaDAO  extends GenericoDAO<Cita>{

  public List<Cita> buscarPorIdMedico(Long id) {
    TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c " +
      "WHERE c.medico.id = :idMedico AND c.fecha=CURRENT_DATE", Cita.class);
    q.setParameter("idMedico", id);
    return q.getResultList();
  }

  public List<Cita> buscarCitasAnteriores(Long idMedico, Long idPaciente) {
    TypedQuery<Cita> q = em.createQuery("SELECT c FROM Cita c " +
      "WHERE c.medico.id = :idMedico AND c.paciente.id = :idPaciente AND c.fecha<CURRENT_DATE", Cita.class);
    q.setParameter("idMedico", idMedico);
    q.setParameter("idPaciente", idPaciente);
    return q.getResultList();
  }

}
