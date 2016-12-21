package es.uvigo.esei.dagss.dominio.services;

import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.entidades.Tratamiento;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Named(value = "prescripcionService")
@SessionScoped
public class PrescripcionService implements Serializable {

    private static final int DIAS_ANTES = -7;
    private static final int DIAS_DESPUES = 15;

    @EJB
    RecetaDAO recetaDAO;

    public PrescripcionService() {
    }

    public void generarRecetas(Tratamiento tratamiento) {
        for (Prescripcion p : tratamiento.getPrescripciones()) {
            generarReceta(p);
        }
    }

    public void generarReceta(Prescripcion p) {
        try {
            long diff = p.getFechaFin().getTime() - p.getFechaInicio().getTime();
            int diasTotales = (int) Math.ceil(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
            double b = ((double) p.getDosis() * diasTotales) / p.getMedicamento().getNumeroDosis();
            int numRecetas = (int) Math.ceil(b);
            int duracion = (int) Math.floor((p.getMedicamento().getNumeroDosis() / p.getDosis()));
            for (int i = 0; i < numRecetas; i++) {
                Receta r = new Receta();
                r.setCantidad(1);
                r.setPrescripcion(p);
                r.setEstadoReceta(EstadoReceta.GENERADA);
                r.setInicioValidez(addDays(p.getFechaInicio(), (duracion * i) + DIAS_ANTES));
                r.setFinValidez(addDays(p.getFechaInicio(), (duracion * i) + DIAS_DESPUES));
                recetaDAO.crear(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void borrarReceta(Prescripcion p) {
        for ( Receta r : recetaDAO.buscarPorIdPrescripcion(p.getId()) ) {
            recetaDAO.eliminar(r);
        }
    }

    private Date addDays(Date date, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, amount);
        return c.getTime();
    }

}