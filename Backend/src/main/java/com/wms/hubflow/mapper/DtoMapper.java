package com.wms.hubflow.mapper;

import com.wms.hubflow.dto.ColisDto;
import com.wms.hubflow.dto.MouvementDto;
import com.wms.hubflow.dto.UtilisateurDto;
import com.wms.hubflow.entity.Colis;
import com.wms.hubflow.entity.Mouvement;
import com.wms.hubflow.entity.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DtoMapper {

    public ColisDto toColisDto(Colis colis) {
        if (colis == null) return null;

        List<MouvementDto> mouvementsDto = colis.getMouvements() != null ?
                colis.getMouvements().stream().map(this::toMouvementDto).toList() :
                Collections.emptyList();

        return ColisDto.builder()
                .id(colis.getId())
                .trackingNumber(colis.getTrackingNumber())
                .codePostalDestinataire(colis.getCodePostalDestinataire())
                .codePays(colis.getCodePays())
                .poidsKg(colis.getPoidsKg())
                .estEndommage(colis.isEstEndommage())
                .statut(colis.getStatut())
                .zoneDestination(colis.getZoneDestination())
                .motifRejet(colis.getMotifRejet())
                .mouvements(mouvementsDto)
                .createdAt(colis.getCreatedAt())
                .updatedAt(colis.getUpdatedAt())
                .build();
    }

    public MouvementDto toMouvementDto(Mouvement mouvement) {
        if (mouvement == null) return null;

        String nomComplet = (mouvement.getOperateur() != null) ?
                mouvement.getOperateur().getPrenom() + " " + mouvement.getOperateur().getNom() : "N/A";

        return MouvementDto.builder()
                .id(mouvement.getId())
                .colisId(mouvement.getColis() != null ? mouvement.getColis().getId() : null)
                .usernameOperateur(mouvement.getOperateur() != null ? mouvement.getOperateur().getUsername() : null)
                .nomOperateurComplete(nomComplet)
                .typeEvenement(mouvement.getTypeEvenement())
                .zoneAssignee(mouvement.getZoneAssignee())
                .emplacementBac(mouvement.getEmplacementBac())
                .commentaire(mouvement.getCommentaire())
                .horodatage(mouvement.getHorodatage())
                .build();
    }

    public UtilisateurDto toUtilisateurDto(Utilisateur utilisateur) {
        if (utilisateur == null) return null;

        return UtilisateurDto.builder()
                .id(utilisateur.getId())
                .username(utilisateur.getUsername())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .actif(utilisateur.isActif())
                .createdAt(utilisateur.getCreatedAt())
                .build();
    }
}