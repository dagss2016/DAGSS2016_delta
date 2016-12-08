/*
 Práctica Java EE 7, DAGSS 2016/17 (ESEI, U. de Vigo)
 */
package es.uvigo.esei.dagss.dominio.entidades;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@SuppressWarnings("unused")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Paciente paciente;

    @Size(max = 255)
    private String descripcion;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fecha;

    @OneToMany(mappedBy = "tratamiento", cascade = CascadeType.ALL)
    private List<Prescripcion> prescripciones = new ArrayList<>();

    public Tratamiento() {}

    public Tratamiento(Paciente paciente, String descripcion, Date fecha) {
        this.paciente = paciente;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.prescripciones = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<Prescripcion> getPrescripciones() {
        return prescripciones;
    }

    public void setPrescripciones(List<Prescripcion> prescripciones) {
        this.prescripciones = prescripciones;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }

        final Prescripcion other = (Prescripcion) obj;
        return Objects.equals(this.id, other.id);
    }
}
