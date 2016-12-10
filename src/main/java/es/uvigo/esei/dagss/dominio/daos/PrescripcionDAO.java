/*
 Proyecto Java EE, DAGSS-2016
 */
package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
@LocalBean
public class PrescripcionDAO extends GenericoDAO<Prescripcion> {

    public Prescripcion buscarPorIdConRecetas(Long id) {
        TypedQuery<Prescripcion> q = em.createQuery("SELECT p FROM Prescripcion AS p JOIN FETCH p.recetas"
                                                  + "  WHERE p.id = :id ORDER BY P.fechaInicio", Prescripcion.class);
        q.setParameter("id", id);

        return q.getSingleResult();
    }

    public List<Prescripcion> buscarPorIdPaciente(Long id) {
        TypedQuery<Prescripcion> q = em.createQuery("SELECT p FROM Prescripcion AS p "
          + "  WHERE p.paciente.id = :id", Prescripcion.class);
        q.setParameter("id", id);

        return q.getResultList();
    }

    public List<Prescripcion> buscarPorIdPacienteAndIdTratamiento(Long idPaciente, Long idTratamiento) {
        TypedQuery<Prescripcion> q = em.createQuery("SELECT p FROM Prescripcion AS p "
                + "  WHERE p.paciente.id = :idP  AND p.tratamiento.id = :idT", Prescripcion.class);
        q.setParameter("idP", idPaciente);
        q.setParameter("idT", idTratamiento);


        return q.getResultList();
    }
}
