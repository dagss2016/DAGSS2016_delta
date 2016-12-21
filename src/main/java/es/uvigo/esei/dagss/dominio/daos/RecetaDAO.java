/*
 Proyecto Java EE, DAGSS-2014
 */

package es.uvigo.esei.dagss.dominio.daos;

import es.uvigo.esei.dagss.dominio.entidades.Receta;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Stateless
@LocalBean
@Named(value = "recetaDAO")
public class RecetaDAO extends GenericoDAO<Receta>{

    public List<Receta> buscarPorTarjetaPaciente(String numeroTarjeta){
        Calendar cal = Calendar.getInstance();
        TypedQuery<Receta> q = em.createQuery("SELECT r FROM Receta r " +
          "WHERE  r.finValidez >= :date AND r.prescripcion.paciente.numeroTarjetaSanitaria = :nTarjeta ORDER BY r.inicioValidez ASC", Receta.class);
        q.setParameter("nTarjeta", numeroTarjeta);
        q.setParameter("date", cal.getTime());
        return q.getResultList();
    }

    public  List<Receta> buscarPorIdPrescripcion(Long id){
        TypedQuery<Receta> q = em.createQuery("SELECT r FROM Receta r " +
                "WHERE r.prescripcion.id >= :id", Receta.class);
        q.setParameter("id", id);
        return q.getResultList();
    }
}
