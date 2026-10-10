package com.wms.hubflow.service;

import com.wms.hubflow.dto.ScanRequestDto;
import com.wms.hubflow.dto.ScanResultDto;
import com.wms.hubflow.entity.Colis;
import com.wms.hubflow.entity.Mouvement;
import com.wms.hubflow.entity.Utilisateur;
import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.TypeEvenement;
import com.wms.hubflow.enums.ZoneLogistique;
import com.wms.hubflow.exception.InvalidScanException;
import com.wms.hubflow.exception.ResourceNotFoundException;
import com.wms.hubflow.repository.ColisRepository;
import com.wms.hubflow.repository.MouvementRepository;
import com.wms.hubflow.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ColisRepository colisRepository;
    private final MouvementRepository mouvementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ScanResultDto traiterScan(ScanRequestDto request) {
        if (request == null || request.trackingNumber() == null || request.trackingNumber().trim().isEmpty()) {
            throw new InvalidScanException("Le numéro de tracking du colis ne peut pas être vide.");
        }

        // 1. Récupération de l'opérateur
        Utilisateur operateur = utilisateurRepository.findByUsername(request.usernameOperateur())
                .orElseThrow(() -> new ResourceNotFoundException("Opérateur introuvable avec l'identifiant : " + request.usernameOperateur()));

        if (!operateur.isActif()) {
            throw new InvalidScanException("L'opérateur " + request.usernameOperateur() + " est désactivé.");
        }

        // 2. Recherche ou création du colis
        Colis colis = colisRepository.findByTrackingNumber(request.trackingNumber().trim())
                .orElseGet(() -> Colis.builder()
                        .trackingNumber(request.trackingNumber().trim())
                        .codePostalDestinataire(request.codePostalDestinataire())
                        .codePays(request.codePays() != null ? request.codePays() : "FR")
                        .poidsKg(request.poidsKg())
                        .build());

        // Mise à jour des informations si de nouvelles données sont apportées lors du scan
        if (request.codePostalDestinataire() != null) {
            colis.setCodePostalDestinataire(request.codePostalDestinataire());
        }
        if (request.codePays() != null) {
            colis.setCodePays(request.codePays());
        }
        if (request.poidsKg() != null) {
            colis.setPoidsKg(request.poidsKg());
        }
        colis.setEstEndommage(request.estEndommage());

        // 3. Calcul de la zone logistique par le moteur de règles
        ZoneLogistique zoneCalculee = determinerZoneDestination(colis);
        colis.setZoneDestination(zoneCalculee);

        // 4. Attribution du statut métier
        if (zoneCalculee == ZoneLogistique.ZONE_99_REJET) {
            colis.setStatut(StatutColis.EN_REJET);
        } else {
            colis.setStatut(StatutColis.EN_TRI);
            colis.setMotifRejet(null); // Réinitialisation du motif en cas de correction
        }

        colis = colisRepository.save(colis);

        // 5. Enregistrement du mouvement dans la table d'audit
        TypeEvenement typeEvt = (zoneCalculee == ZoneLogistique.ZONE_99_REJET)
                ? TypeEvenement.SIGNALEMENT_ANOMALIE
                : TypeEvenement.ORIENTE_TRI;

        Mouvement mouvement = Mouvement.builder()
                .colis(colis)
                .operateur(operateur)
                .typeEvenement(typeEvt)
                .zoneAssignee(zoneCalculee)
                .emplacementBac(request.emplacementBac())
                .commentaire(request.commentaire())
                .build();

        colis.addMouvement(mouvement);
        mouvementRepository.save(mouvement);

        ScanResultDto result = ScanResultDto.builder()
                .trackingNumber(colis.getTrackingNumber())
                .zoneDestination(zoneCalculee)
                .libelleZone(zoneCalculee.getLibelle())
                .codeHub(zoneCalculee.getCodeHub())
                .statut(colis.getStatut())
                .estRejet(zoneCalculee == ZoneLogistique.ZONE_99_REJET)
                .motifRejet(colis.getMotifRejet())
                .horodatage(LocalDateTime.now())
                .build();

        // 6. Notification WebSocket en temps réel
        try {
            messagingTemplate.convertAndSend("/topic/scans", result);
        } catch (Exception e) {
            // Log d'avertissement si le broker WebSocket échoue, n'annule pas la transaction BDD
        }

        return result;
    }

    /**
     * Moteur de calcul de destination selon la géographie française (15 zones), internationale et anomalies.
     */
    public ZoneLogistique determinerZoneDestination(Colis colis) {
        if (colis.isEstEndommage()) {
            colis.setMotifRejet("COLIS_ENDOMMAGE");
            return ZoneLogistique.ZONE_99_REJET;
        }

        String pays = colis.getCodePays();
        if (pays != null && !pays.trim().equalsIgnoreCase("FR")) {
            return ZoneLogistique.ZONE_16_ETRANGER;
        }

        String cp = colis.getCodePostalDestinataire();
        if (cp == null || !cp.matches("^[0-9]{5}$")) {
            colis.setMotifRejet("CODE_POSTAL_INVALIDE");
            return ZoneLogistique.ZONE_99_REJET;
        }

        String dept = cp.substring(0, 2);

        return switch (dept) {
            case "75", "93", "95" -> ZoneLogistique.ZONE_01_IDF_NORD;
            case "77", "78", "91", "92", "94" -> ZoneLogistique.ZONE_02_IDF_SUD;
            case "59", "62" -> ZoneLogistique.ZONE_03_NORD;
            case "08", "10", "51", "52", "54", "55", "57", "67", "68", "88" -> ZoneLogistique.ZONE_04_GRAND_EST;
            case "14", "27", "50", "61", "76" -> ZoneLogistique.ZONE_05_NORMANDIE;
            case "22", "29", "35", "56" -> ZoneLogistique.ZONE_06_BRETAGNE;
            case "18", "28", "36", "37", "41", "44", "45", "49", "53", "72", "85" -> ZoneLogistique.ZONE_07_PAYS_DE_LOIRE;
            case "21", "25", "39", "58", "70", "71", "89", "90" -> ZoneLogistique.ZONE_08_BOURGOGNE;
            case "01", "03", "07", "26", "38", "42", "69", "73", "74" -> ZoneLogistique.ZONE_09_AURA;
            case "16", "17", "24", "33", "79", "86" -> ZoneLogistique.ZONE_10_NOUVELLE_AQUITAINE_NORD;
            case "40", "64" -> ZoneLogistique.ZONE_11_NOUVELLE_AQUITAINE_SUD;
            case "09", "31", "32", "46", "65", "81", "82" -> ZoneLogistique.ZONE_12_OCCITANIE_OUEST;
            case "11", "30", "34", "48", "66" -> ZoneLogistique.ZONE_13_OCCITANIE_EST;
            case "04", "05", "06", "13", "83", "84", "20", "2A", "2B" -> ZoneLogistique.ZONE_14_PACA_CORSE;
            case "12", "15", "19", "23", "43", "63", "87" -> ZoneLogistique.ZONE_15_MASSIF_CENTRAL;
            default -> {
                colis.setMotifRejet("DEPARTEMENT_INCONNU");
                yield ZoneLogistique.ZONE_99_REJET;
            }
        };
    }
}