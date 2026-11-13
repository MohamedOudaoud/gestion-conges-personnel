package com.gestconges.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "soldes_conge",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"employe_id", "type_conge_id", "annee"}
                )
        }
)
public class SoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(nullable = false)
    private Integer annee;

    private Integer joursAcquis;

    private Integer joursPris;

    // --- Constructors ---

    public SoldeConge() {
    }

    public SoldeConge(Employe employe, TypeConge typeConge, Integer annee, Integer joursAcquis, Integer joursPris) {
        this.employe = employe;
        this.typeConge = typeConge;
        this.annee = annee;
        this.joursAcquis = joursAcquis;
        this.joursPris = joursPris;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public TypeConge getTypeConge() {
        return typeConge;
    }

    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public Integer getJoursAcquis() {
        return joursAcquis;
    }

    public void setJoursAcquis(Integer joursAcquis) {
        this.joursAcquis = joursAcquis;
    }

    public Integer getJoursPris() {
        return joursPris;
    }

    public void setJoursPris(Integer joursPris) {
        this.joursPris = joursPris;
    }
}