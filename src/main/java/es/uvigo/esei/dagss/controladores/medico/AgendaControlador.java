package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.dominio.daos.CitaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Cita;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class AgendaControlador implements Serializable {

    @Inject
    CitaDAO citaDAO;
    @Inject
    MedicoControlador medicoControlador;
    Cita citaActual;
    private List<Cita> citas;

    public AgendaControlador() {}

    @PostConstruct
    public void inicializar() {
        citas = citaDAO.buscarPorIdMedico(medicoControlador.getMedicoActual().getId());
    }

    public Cita getCitaActual() {
        return citaActual;
    }

    public void setCitaActual(Cita citaActual) {
        this.citaActual = citaActual;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

}
