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
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private String dni;
    private String password;
    private Medico medicoActual;
    private String numeroColegiado;
    private List<Cita> citasAnteriores;

    private String descripcionTratamiento;
    private Tratamiento tratamientoActual;
    private List<Tratamiento> tratamientos;
    private Prescripcion prescripcionActual;
    private List<Prescripcion> prescripciones;

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

    public String getDescripcionTratamiento() {
        return descripcionTratamiento;
    }

    public void setDescripcionTratamiento(String descripcionTratamiento) {
        this.descripcionTratamiento = descripcionTratamiento;
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
        inicializarVariables();
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

    public void doNuevoTratamiento() {
        tratamientoActual = new Tratamiento();
        descripcionTratamiento = "";
        inicializarPrescripciones();
        inicializarPrescripcionActual();
    }

    public void doGuardarTratamiento(Paciente paciente) {
        tratamientoActual.setPrescripciones(prescripciones);
        Tratamiento t = tratamientoDAO.actualizar(tratamientoActual);
        prescripcionService.generarRecetas(t);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
        tratamientoActual = null;
        inicializarPrescripcionActual();
    }

    public void doActualizarTratamiento(Paciente paciente){
        tratamientoDAO.actualizar(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
        tratamientoActual = null;
        inicializarPrescripcionActual();
    }

    public void doEditarTratamiento() {
        prescripcionActual = new Prescripcion();
    }

    public void doBorrarTratamiento() {
        tratamientoDAO.eliminar(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(tratamientoActual.getPaciente().getId());
        tratamientoActual = null;
    }

    public void doNuevaPrescripcion(Paciente paciente) {
        if (tratamientoActual.getId() == null) {
            tratamientoActual.setFecha(Calendar.getInstance().getTime());
            tratamientoActual.setPaciente(paciente);
            tratamientoActual.setDescripcion(descripcionTratamiento);
            tratamientoDAO.crear(tratamientoActual);
        }
        prescripcionActual.setMedico(medicoActual);
        prescripcionActual.setTratamiento(tratamientoActual);
        prescripcionActual.setPaciente(paciente);
        prescripcionDAO.crear(prescripcionActual);
        prescripcionService.generarReceta(prescripcionActual);
        tratamientoActual.getPrescripciones().add(prescripcionActual);
        tratamientoDAO.actualizar(tratamientoActual);
        prescripciones = prescripcionDAO.buscarPorIdPacienteAndIdTratamiento(paciente.getId(), tratamientoActual.getId());
        inicializarPrescripcionActual();
    }

    public void doBorrarPrescripcion() {
        for ( Prescripcion prescripcion : tratamientoActual.getPrescripciones() ) {
            if( prescripcionActual.getId().equals(prescripcion.getId()) ) {
                tratamientoActual.getPrescripciones().remove(prescripcion);
                prescripcionService.borrarReceta(prescripcion);
            }
        }
    }

    public void doBorrarPrescripcion(Prescripcion p) {
        if(!prescripciones.isEmpty()) {
            prescripciones.remove(p);
        }
    }

    public List<Medicamento> doBuscarMedicamentosPorCadena(String cadena) {
        return medicamentoDAO.buscarPorDescripcion(cadena);
    }

    public void inicializarVariables() {
        inicializarDescripcionTratamiento();
        inicializarTratamientoActual();
        inicializarTratamientos();
        inicializarPrescripciones();
        inicializarPrescripcionActual();
    }

    private void inicializarTratamientos() {
        tratamientos = new ArrayList<>();
    }

    private void inicializarPrescripciones() {
        prescripciones = new ArrayList<>();
    }

    private void inicializarDescripcionTratamiento() {
        descripcionTratamiento = "";
    }

    private void inicializarTratamientoActual() {
        tratamientoActual = new Tratamiento();
    }

    private void inicializarPrescripcionActual() {
        prescripcionActual = new Prescripcion();
    }

    @SuppressWarnings("unused")
    public void doCerrarDialgoNuevoTratamiento() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Cierro dialogo nuevo."));
    }

    @SuppressWarnings("unused")
    public void doCerrarDialgoVerTratamiento() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Cierro dialogo ver."));
    }

    @SuppressWarnings("unused")
    public void doCerrarDialgoEditarTratamiento() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Cierro dialogo editar."));
    }

}
