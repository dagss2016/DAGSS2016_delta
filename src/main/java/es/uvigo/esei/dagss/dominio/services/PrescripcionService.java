package es.uvigo.esei.dagss.dominio.services;

import es.uvigo.esei.dagss.dominio.daos.PrescripcionDAO;
import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.entidades.Tratamiento;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class PrescripcionService {

    @Inject
    RecetaDAO recetaDAO;
    @Inject
    PrescripcionDAO prescripcionDAO;

    public PrescripcionService() {}

    public void generarRecetas(Tratamiento tratamiento) {

        for ( Prescripcion p: tratamiento.getPrescripciones() ) {
            int dias = (int) Math.floor(p.getMedicamento().getNumeroDosis() / p.getDosis());
            long diff =  p.getFechaFin().getTime() - p.getFechaInicio().getTime();
            int diasTotales = (int) Math.ceil(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
            int numRecetas = (int) Math.ceil(diasTotales / dias);
            Receta r = new Receta();
            r.setCantidad(numRecetas);
            r.setEstado(EstadoReceta.GENERADA);
            r.setEstadoReceta(EstadoReceta.GENERADA);
            r.setPrescripcion(p);
            r.setInicioValidez(p.getFechaInicio());
            r.setFinValidez(p.getFechaFin());
            recetaDAO.crear(r);
        }
    }

}