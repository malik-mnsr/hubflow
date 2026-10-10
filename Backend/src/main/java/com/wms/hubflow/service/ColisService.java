package com.wms.hubflow.service;

import com.wms.hubflow.dto.ColisDto;
import com.wms.hubflow.dto.ZoneStatDto;
import com.wms.hubflow.entity.Colis;
import com.wms.hubflow.entity.Mouvement;
import com.wms.hubflow.entity.Utilisateur;
import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.TypeEvenement;
import com.wms.hubflow.enums.ZoneLogistique;
import com.wms.hubflow.exception.DuplicateResourceException;
import com.wms.hubflow.exception.ResourceNotFoundException;
import com.wms.hubflow.repository.ColisRepository;
import com.wms.hubflow.repository.MouvementRepository;
import com.wms.hubflow.repository.UtilisateurRepository;
import com.wms.hubflow.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColisService {

    private final ColisRepository colisRepository;
    private final MouvementRepository mouvementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ScanService scanService;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public ColisDto obtenirParTrackingNumber(String trackingNumber) {
        Colis colis = colisRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun colis trouvé avec le tracking : " + trackingNumber));
        return dtoMapper.toColisDto(colis);
    }

    @Transactional(readOnly = true)
    public List<ColisDto> obtenirParZone(ZoneLogistique zone) {
        return colisRepository.findByZoneDestination(zone)
                .stream()
                .map(dtoMapper::toColisDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneStatDto> obtenirStatistiquesDashboard() {
        // Récupérer les totaux groupés par zone depuis la BDD
        List<Object[]> rawStats = colisRepository.countColisByZone();
        Map<ZoneLogistique, Long> countsMap = rawStats.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> (ZoneLogistique) row[0],
                        row -> (Long) row[1]
                ));

        // Construire la liste complète pour les 17 zones (même celles à 0)
        List<ZoneStatDto> statsList = new ArrayList<>();
        for (ZoneLogistique zone : ZoneLogistique.values()) {
            long total = countsMap.getOrDefault(zone, 0L);
            boolean estAlerte = (zone == ZoneLogistique.ZONE_99_REJET);

            statsList.add(ZoneStatDto.builder()
                    .zone(zone)
                    .libelleZone(zone.getLibelle())
                    .codeHub(zone.getCodeHub())
                    .totalColis(total)
                    .estAlerte(estAlerte)
                    .build());
        }

        return statsList;
    }

    /**
     * Permet au superviseur de corriger manuellement une anomalie sur la Zone 99
     */
    @Transactional
    public ColisDto corrigerAnomalie(String trackingNumber, String nouveauCodePostal, String usernameSuperviseur) {
        Colis colis = colisRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable : " + trackingNumber));

        Utilisateur superviseur = utilisateurRepository.findByUsername(usernameSuperviseur)
                .orElseThrow(() -> new ResourceNotFoundException("Superviseur introuvable : " + usernameSuperviseur));

        colis.setCodePostalDestinataire(nouveauCodePostal);
        colis.setEstEndommage(false); // Réinitialisation de l'état endommagé

        // Recalcul de la zone
        ZoneLogistique nouvelleZone = scanService.determinerZoneDestination(colis);
        colis.setZoneDestination(nouvelleZone);

        if (nouvelleZone == ZoneLogistique.ZONE_99_REJET) {
            colis.setStatut(StatutColis.EN_REJET);
        } else {
            colis.setStatut(StatutColis.EN_TRI);
            colis.setMotifRejet(null);
        }

        colis = colisRepository.save(colis);

        // Mouvement de correction manuelle
        Mouvement mouvement = Mouvement.builder()
                .colis(colis)
                .operateur(superviseur)
                .typeEvenement(TypeEvenement.CORRECTION_MANUELLE)
                .zoneAssignee(nouvelleZone)
                .commentaire("Correction manuelle du code postal : " + nouveauCodePostal)
                .build();

        mouvementRepository.save(mouvement);

        return dtoMapper.toColisDto(colis);
    }
}