package com.wms.hubflow.entity;

import com.wms.hubflow.enums.TypeEvenement;
import com.wms.hubflow.enums.ZoneLogistique;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements", indexes = {
        @Index(name = "idx_mouvements_colis", columnList = "colis_id"),
        @Index(name = "idx_mouvements_operateur", columnList = "operateur_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colis_id", nullable = false)
    private Colis colis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operateur_id", nullable = false)
    private Utilisateur operateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 30)
    private TypeEvenement typeEvenement;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_assignee", length = 40)
    private ZoneLogistique zoneAssignee;

    @Column(name = "emplacement_bac", length = 50)
    private String emplacementBac;

    @Column(length = 255)
    private String commentaire;

    @Column(nullable = false, updatable = false)
    private LocalDateTime horodatage;

    @PrePersist
    protected void onCreate() {
        this.horodatage = LocalDateTime.now();
    }
}