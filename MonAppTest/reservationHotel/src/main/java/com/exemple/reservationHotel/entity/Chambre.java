package com.exemple.reservationHotel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author mlahy
 */
@Entity
@Table(name = "chambres")
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarif_par_nuit", nullable = false)
    private Double tarifParNuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_chambre", nullable = false)
    private TypeChambre typeChambre;

    @Column(name = "numero_chambre", nullable = false, unique = true)
    private String numeroChambre;  // Champ pour le numéro de chambre

    // Constructeurs
    public Chambre() {
    }

    public Chambre(Double tarifParNuit, TypeChambre typeChambre) {
        this.tarifParNuit = tarifParNuit;
        this.typeChambre = typeChambre;
        this.numeroChambre = genererNumeroChambre();  // Génération automatique du numéro
    }

    // Méthode pour générer un numéro de chambre basé sur le type
    private String genererNumeroChambre() {
        switch (this.typeChambre) {
            case SIMPLE:
                return "S" + this.id;
            case DOUBLE:
                return "D" + this.id;
            case SUITE:
                return "T" + this.id;
            default:
                return "G" + this.id;  // Par défaut pour une chambre générique
        }
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTarifParNuit() {
        return tarifParNuit;
    }

    public void setTarifParNuit(Double tarifParNuit) {
        this.tarifParNuit = tarifParNuit;
    }

    public TypeChambre getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(TypeChambre typeChambre) {
        this.typeChambre = typeChambre;
        this.numeroChambre = genererNumeroChambre();  // Mise à jour du numéro lors du changement de type
    }

    public String getNumeroChambre() {
        return numeroChambre;
    }

    public void setNumeroChambre(String numeroChambre) {
        this.numeroChambre = numeroChambre;
    }
}
