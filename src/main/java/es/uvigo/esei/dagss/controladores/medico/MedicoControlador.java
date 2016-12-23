/*
 Proyecto Java EE, DAGSS-2016
 */
package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.*;
import es.uvigo.esei.dagss.dominio.entidades.*;
import es.uvigo.esei.dagss.dominio.services.PrescripcionService;
import org.primefaces.context.RequestContext;

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
import java.util.Date;
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
                    setMedicoActual(medico);
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }

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
        inicializarVariables();
    }

    public void doGuardarTratamiento(Paciente paciente) {
        if (!descripcionTratamiento.isEmpty()) {
            tratamientoActual.setPrescripciones(prescripciones);
            tratamientoActual.setDescripcion(descripcionTratamiento);
            Tratamiento t = tratamientoDAO.actualizar(tratamientoActual);
            prescripcionService.generarRecetas(t);
            tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('DialogoNuevo').hide();");
            resetearVariables();

            FacesContext.getCurrentInstance().addMessage("detallesCitaMensajes",
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
                            "Tratamiento añadido correctamente."));
        } else {

            FacesContext.getCurrentInstance().addMessage("dialogoNuevoMensajes",
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                            "Es necesario indicar un nombre para el tratamiento."));

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('DialogoNuevo').show();");

        }
    }

    public void doActualizarTratamiento(Paciente paciente){

        List<Prescripcion> toRemove = new ArrayList<>();

        for (Prescripcion p : prescripciones) {
            if (!tratamientoActual.getPrescripciones().contains(p)){
                tratamientoActual.getPrescripciones().add(p);
                prescripcionDAO.crear(p);
                prescripcionService.generarReceta(p);
            }
        }

        for (Prescripcion p : tratamientoActual.getPrescripciones()) {
            if (!prescripciones.contains(p)){
                toRemove.add(p);
            }
        }

        for (Prescripcion p : toRemove ) {
            tratamientoActual.getPrescripciones().remove(p);
            prescripcionDAO.eliminar(p);
            prescripcionService.borrarReceta(p);
        }

        tratamientoDAO.actualizar(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(paciente.getId());
        resetearVariables();

        FacesContext.getCurrentInstance().addMessage("detallesCitaMensajes",
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
                        "Tratamiento modificado correctamente."));
    }

    public void doEditarTratamiento() {
        inicializarPrescripcionActual();
        inicializarPrescripciones();
        for (Prescripcion p: tratamientoActual.getPrescripciones()) {
            prescripciones.add(p);
        }
    }

    public void doBorrarTratamiento() {
        tratamientoDAO.eliminar(tratamientoActual);
        tratamientos = tratamientoDAO.buscarPorIDPaciente(tratamientoActual.getPaciente().getId());
        resetearTratamientoActual();

        FacesContext.getCurrentInstance().addMessage("detallesCitaMensajes",
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
                        "Tratamiento eliminado correctamente."));
    }

    public void doCancelarNuevoTratamiento(){
        tratamientoDAO.eliminar(tratamientoActual);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('DialogoNuevo').hide();");
        resetearVariables();
    }
    public void doCancelarEditarTratamiento(){
        tratamientos = tratamientoDAO.buscarPorIDPaciente(tratamientoActual.getPaciente().getId());
        resetearVariables();
    }

    public void doNuevaPrescripcion(Paciente paciente) {
        if (validarFechas()) {
            crearTratamientoIfNull(paciente);
            crearPrescripcion(paciente);
            resetearPrescripcionActual();
        } else {
            FacesContext.getCurrentInstance().addMessage("dialogoNuevoMensajes",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "La fecha de fin tiene que ser mayor que la de inicio."));
        }
    }

    public void doNuevaPrescripcionEditar(Paciente paciente) {
        if (validarFechas()) {
            prescripcionActual.setMedico(getMedicoActual());
            prescripcionActual.setTratamiento(tratamientoActual);
            prescripcionActual.setPaciente(paciente);
            prescripciones.add(prescripcionActual);
            resetearPrescripcionActual();
        } else {
            FacesContext.getCurrentInstance().addMessage("dialogoEditarMensajes",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "La fecha de fin tiene que ser mayor que la de inicio."));
        }
    }

    public void doBorrarPrescripcion(Prescripcion p) {
        if(!prescripciones.isEmpty() ) {
            prescripciones.remove(p);
        }
    }

    public List<Medicamento> doBuscarMedicamentosPorCadena(String cadena) {
        return medicamentoDAO.buscarPorDescripcion(cadena);
    }

    public Date today(){
        return Calendar.getInstance().getTime();
    }

    private boolean validarFechas() {
        return prescripcionActual.getFechaFin().getTime() > prescripcionActual.getFechaInicio().getTime();
    }

    private void inicializarVariables() {
        inicializarTratamientoActual();
        inicializarPrescripciones();
        inicializarPrescripcionActual();
        inicializarDescripcionTratamiento();
    }

    private void resetearVariables(){
        resetearTratamientoActual();
        resetearPrescripciones();
        resetearPrescripcionActual();
        resetearDescripcionTratamiento();
    }

    private void crearTratamientoIfNull(Paciente paciente){
        if (tratamientoActual.getId() == null) {
            tratamientoActual.setFecha(Calendar.getInstance().getTime());
            tratamientoActual.setPaciente(paciente);
            tratamientoActual.setDescripcion("");
            tratamientoActual = tratamientoDAO.crear(tratamientoActual);
        }
    }

    private void crearPrescripcion(Paciente paciente){
        prescripcionActual.setMedico(getMedicoActual());
        prescripcionActual.setTratamiento(tratamientoActual);
        prescripcionActual.setPaciente(paciente);
        prescripcionDAO.crear(prescripcionActual);
        prescripciones.add(prescripcionActual);
    }

    private void inicializarPrescripciones() {
        prescripciones = new ArrayList<>();
    }

    private void inicializarTratamientoActual() {
        tratamientoActual = new Tratamiento();
    }

    private void inicializarPrescripcionActual() {
        prescripcionActual = new Prescripcion();
    }

    private void inicializarDescripcionTratamiento() {
        descripcionTratamiento = "";
    }

    private void resetearPrescripciones() {
        prescripciones = null;
    }

    private void resetearTratamientoActual() {
        tratamientoActual = null;
    }

    private void resetearPrescripcionActual() {
        prescripcionActual = null;
    }

    private void resetearDescripcionTratamiento() {
        descripcionTratamiento = null;
    }

}
