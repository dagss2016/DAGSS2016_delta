package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.dominio.daos.CitaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Cita;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AgendaControlador implements Serializable {

  private List<Cita> citas;

  @Inject
  CitaDAO citaDAO;

  @Inject
  MedicoControlador medicoControlador;

  Cita citaActual;

  public Cita getCitaActual() {
    return citaActual;
  }

  public void setCitaActual(Cita citaActual) {
    this.citaActual = citaActual;
  }

  public AgendaControlador(){
  }
  @PostConstruct
  public void inicializar() {
    citas = citaDAO.buscarPorIdMedico(medicoControlador.getMedicoActual().getId());
  }

  public List<Cita> getCitas() {
    return citas;
  }

  public void setCitas(List<Cita> citas) {
    this.citas = citas;
  }

}
