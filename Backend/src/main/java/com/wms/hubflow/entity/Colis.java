package com.wms.hubflow.entity;

import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.ZoneLogistique;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colis", indexes = {
        @Index(name = "idx_colis_tracking", columnList = "trackingNumber"),
        @Index(name = "idx_colis_zone", columnList = "zoneDestination"),
        @Index(name = "idx_colis_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Colis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 64)
    @EqualsAndHashCode.Include
    private String trackingNumber;

    @Column(name = "code_postal_destinataire", length = 10)
    private String codePostalDestinataire;

    @Builder.Default
    @Column(name = "code_pays", nullable = false, length = 3)
    private String codePays = "FR";

    @Column(name = "poids_kg")
    private Double poidsKg;

    @Builder.Default
    @Column(name = "est_endommage", nullable = false)
    private boolean estEndommage = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutColis statut = StatutColis.CREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_destination", length = 40)
    private ZoneLogistique zoneDestination;

    @Column(name = "motif_rejet", length = 255)
    private String motifRejet;

    @Builder.Default
    @OneToMany(mappedBy = "colis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mouvement> mouvements = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addMouvement(Mouvement mouvement) {
        mouvements.add(mouvement);
        mouvement.setColis(this);
    }
}