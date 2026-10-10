package com.wms.hubflow.repository;

import com.wms.hubflow.entity.Colis;
import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.ZoneLogistique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColisRepository extends JpaRepository<Colis, Long> {

    Optional<Colis> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    List<Colis> findByStatut(StatutColis statut);

    List<Colis> findByZoneDestination(ZoneLogistique zoneDestination);

    long countByZoneDestination(ZoneLogistique zoneDestination);

    long countByStatut(StatutColis statut);

    // Requête agrégée pour alimenter les statistiques du Dashboard Angular
    @Query("SELECT c.zoneDestination, COUNT(c) FROM Colis c GROUP BY c.zoneDestination")
    List<Object[]> countColisByZone();
}