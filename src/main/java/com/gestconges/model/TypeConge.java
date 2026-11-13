package com.gestconges.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "types_conge")
public class TypeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String libelle;

    private Integer joursParAn;

    private boolean decompteSolde;

    private boolean justificatifRequis;

    private boolean actif = true;

    @OneToMany(mappedBy = "typeConge")
    private List<DemandeConge> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "typeConge")
    private List<SoldeConge> soldes = new ArrayList<>();

    // --- Constructors ---

    public TypeConge() {
    }

    public TypeConge(String libelle, Integer joursParAn, boolean decompteSolde, boolean justificatifRequis) {
        this.libelle = libelle;
        this.joursParAn = joursParAn;
        this.decompteSolde = decompteSolde;
        this.justificatifRequis = justificatifRequis;
        this.actif = true;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getJoursParAn() {
        return joursParAn;
    }

    public void setJoursParAn(Integer joursParAn) {
        this.joursParAn = joursParAn;
    }

    public boolean isDecompteSolde() {
        return decompteSolde;
    }

    public void setDecompteSolde(boolean decompteSolde) {
        this.decompteSolde = decompteSolde;
    }

    public boolean isJustificatifRequis() {
        return justificatifRequis;
    }

    public void setJustificatifRequis(boolean justificatifRequis) {
        this.justificatifRequis = justificatifRequis;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<DemandeConge> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<DemandeConge> demandes) {
        this.demandes = demandes;
    }

    public List<SoldeConge> getSoldes() {
        return soldes;
    }

    public void setSoldes(List<SoldeConge> soldes) {
        this.soldes = soldes;
    }
}