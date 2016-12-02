package es.uvigo.esei.dagss.controladores.farmacia;

import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import es.uvigo.esei.dagss.dominio.entidades.EstadoReceta;
import es.uvigo.esei.dagss.dominio.entidades.Receta;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class VentaControlador implements Serializable{

    private String numeroTarjeta;
    private List<Receta> recetas;

    @Inject
    RecetaDAO recetaDAO;

    @Inject
    FarmaciaControlador farmaciaControlador;

    public VentaControlador(){}

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public List<Receta> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<Receta> recetas) {
        this.recetas = recetas;
    }

    public void doBuscarTarjeta(){
        recetas = recetaDAO.buscarPorTarjetaPaciente(numeroTarjeta);

    }
    public void doVenderReceta(Receta receta){
        receta.setEstadoReceta(EstadoReceta.SERVIDA);
        receta.setFarmaciaDispensadora(farmaciaControlador.getFarmaciaActual());
        recetaDAO.actualizar(receta);
        recetas = recetaDAO.buscarPorTarjetaPaciente(numeroTarjeta);

    }
}
