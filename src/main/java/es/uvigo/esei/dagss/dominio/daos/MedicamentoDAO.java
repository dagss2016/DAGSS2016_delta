/*
 Proyecto Java EE, DAGSS-2014
 */
package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Medicamento;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
@LocalBean
public class MedicamentoDAO extends GenericoDAO<Medicamento> {

    public List<Medicamento> buscarPorDescripcion(String description) {
        TypedQuery<Medicamento> q = em.createQuery("SELECT m FROM Medicamento AS m "
                + "  WHERE m.nombre LIKE :d or m.principioActivo like :d", Medicamento.class);
        q.setParameter("d", "%" + description + "%");

        return q.getResultList();
    }
}
