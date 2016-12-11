/*
 Proyecto Java EE, DAGSS-2016
 */
package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.*;
import es.uvigo.esei.dagss.dominio.entidades.*;
import es.uvigo.esei.dagss.dominio.services.PrescripcionService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
@Named(value = "medicoControlador")
@SessionScoped
public class MedicoControlador implements Serializable {

    @Inject
    CitaDAO citaDAO;
    @Inject
    PrescripcionDAO prescripcionDAO;
    @Inject
    TratamientoDAO tratamientoDAO;
    @Inject
    MedicamentoDAO medicamentoDAO;
    @Inject
    PrescripcionService prescripcionService;
    @Inject
    private AutenticacionControlador autenticacionControlador;
    @EJB
    private MedicoDAO medicoDAO;
    private Medico medicoActual;
    private String dni;
    private String numeroColegiado;
    private String password;
    private List<Cita> citasAnteriores;
    private List<Tratamiento> tratamientos;
    private Tratamiento tratamientoActual;
    private Prescripcion prescripcionActual;
    private List<Prescripcion> prescripciones;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public MedicoControlador() {
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroColegiado() {
        return numeroColegiado;
    }

    public void setNumeroColegiado(String numeroColegiado) {
        this.numeroColegiado = numeroColegiado;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Medico getMedicoActual() {
        return medicoActual;
    }

    public void setMedicoActual(Medico medicoActual) {
        this.medicoActual = medicoActual;
    }

    public Tratamiento getTratamientoActual() {
        return tratamientoActual;
    }

    public void setTratamientoActual(Tratamiento tratamientoActual) {
        this.tratamientoActual = tratamientoActual;
    }

    public Prescripcion getPrescripcionActual() {
        return prescripcionActual;
    }

    public void setPrescripcionActual(Prescripcion prescripcionActual) {
        this.prescripcionActual = prescripcionActual;
    }

    public List<Cita> getCitasAnteriores() {
        return citasAnteriores;
    }

    public void setCitasAnteriores(List<Cita> citasAnteriores) {
        this.citasAnteriores = citasAnteriores;
    }

    public List<Tratamiento> getTratamientos() {
        return tratamientos;
    }

    public void setTratamientos(List<Tratamiento> tratamientos) {
        this.tratamientos = tratamientos;
    }

    public List<Prescripcion> getPrescripciones() {
        return prescripciones;
    }

    public void setPrescripciones(List<Prescripcion> prescripciones) {
        this.prescripciones = prescripciones;
    }

    private boolean parametrosAccesoInvalidos() {
        return (((dni == null) && (numeroColegiado == null)) || (password == null));
    }

    private Medico recuperarDatosMedico() {
        Medico medico = null;
        if (dni != null) {
            medico = medicoDAO.buscarPorDNI(dni);
        }
        if ((medico == null) && (numeroColegiado != null)) {
            medico = medicoDAO.buscarPorNumeroColegiado(numeroColegiado);
        }
        return medico;
    }


    @PostConstruct
    public void inicializar() {
        prescripciones = new ArrayList<>();
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado un DNI ó un número de colegiado o una contraseña", ""));
        } else {
            Medico medico = recuperarDatosMedico();
            if (medico == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningún médico con los datos indicados", ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(medico.getId(), password,
                        TipoUsuario.MEDICO.getEtiqueta().toLowerCase())) {
                    medicoActual = medico;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }

    //Acciones

    public String doShowCita(Cita citaActual) {
        tratamientos = tratamientoDAO.buscarPorIDPaciente(citaActual.getPaciente().getId());
        citasAnteriores = citaDAO.buscarCitasAnteriores(citaActual.getMedico().getId(), citaActual.getPaciente().getId());
        return "detallesCita";
    }

    public String finalizarCitaCompletada(Cita citaActual) {
        citaActual.setEstado(EstadoCita.COMPLETADA);
        this.citaDAO.actualizar(citaActual);
        return "index";
    }

    public String finalizarCitaPacienteAusente(Cita citaActual) {
        citaActual.setEstado(EstadoCita.AUSENTE);
        this.citaDAO.actualizar(citaActual);
        return "index";
    }

    public void doGuardarTratamiento(Paciente paciente) {
        tratamientoActual.setFecha(Calendar.getInstance().getTime());
        tratamientoActual.setPaciente(paciente);
        tratamientoActual.setPrescripciones(prescripciones);
        tratamientoDAO.actualizar(tratamientoActual);
        prescripcionService.generarRecetas(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
        tratamientoActual = null;
        clear();
    }

    public void doActualizarTratamiento(Paciente paciente){
        tratamientoDAO.actualizar(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
        tratamientoActual = null;
        clear();
    }

    public void doNewTratamiento() {

        tratamientoActual = new Tratamiento();
        clear();
    }

    public void doEditTratamiento() {

        for (Prescripcion p : tratamientoActual.getPrescripciones()) {
            if (p.getId().equals(prescripcionActual.getId())) {
                tratamientoActual.getPrescripciones().remove(p);
                tratamientoActual.getPrescripciones().add(prescripcionActual);
                break;
            }
        }

        tratamientoDAO.actualizar(tratamientoActual);
        doNewPrescripcion();
    }

    public String doDeleteTratamiento(Tratamiento tratamiento) {
        tratamientoDAO.eliminar(tratamiento);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(tratamiento.getPaciente().getId());
        tratamientoActual = null;
        return "detallesCita";
    }

    public void doNewPrescripcion() {
        prescripcionActual = new Prescripcion();
    }

    public void doGuardarPrescripcion(Paciente paciente) {
        prescripcionActual.setMedico(medicoActual);
        prescripcionActual.setTratamiento(tratamientoActual);
        prescripcionActual.setPaciente(paciente);
        prescripcionDAO.crear(prescripcionActual);
        tratamientoActual.getPrescripciones().add(prescripcionActual);
        tratamientoDAO.actualizar(tratamientoActual);
        clear();
    }

    public void doGuardarrPrescripcion(Paciente paciente) {
        if (tratamientoActual.getId() == null) {
            tratamientoActual.setFecha(Calendar.getInstance().getTime());
            tratamientoActual.setPaciente(paciente);
            tratamientoActual.setDescripcion("");
            tratamientoDAO.crear(tratamientoActual);
        }
        prescripcionActual.setMedico(medicoActual);
        prescripcionActual.setTratamiento(tratamientoActual);
        prescripcionActual.setPaciente(paciente);
        prescripcionDAO.crear(prescripcionActual);
        prescripciones.add(prescripcionActual);
        prescripciones = prescripcionDAO.buscarPorIdPacienteAndIdTratamiento(paciente.getId(), tratamientoActual.getId());
        clear();
    }

    public void clear() {
        prescripcionActual = new Prescripcion();
    }

    public List<Medicamento> completeMedicamentos(String descripcion) {
        return medicamentoDAO.buscarPorDescripcion(descripcion);
    }

    public void doDeletePrescripcion() {

        for ( Prescripcion prescripcion : tratamientoActual.getPrescripciones() ) {
            if( prescripcionActual.getId().equals(prescripcion.getId()) ) {
                tratamientoActual.getPrescripciones().remove(prescripcion);
            }
        }
    }

    public List<Medicamento> todosMedicamentos() {
        return medicamentoDAO.buscarTodos();
    }
}
