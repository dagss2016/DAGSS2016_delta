/*
 Proyecto Java EE, DAGSS-2016
 */
package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Tratamiento;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
@LocalBean
public class TratamientoDAO extends GenericoDAO<Tratamiento> {

    public List<Tratamiento> buscarPorIDPaciente(Long id) {
        TypedQuery<Tratamiento> q = em.createQuery("SELECT t FROM Tratamiento AS t "
                + "  WHERE t.paciente.id = :id ORDER BY t.fecha ASC", Tratamiento.class);
        q.setParameter("id", id);

        return q.getResultList();
    }

}
