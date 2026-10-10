package com.wms.hubflow.dto;

import com.wms.hubflow.enums.TypeEvenement;
import com.wms.hubflow.enums.ZoneLogistique;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MouvementDto(
        Long id,
        Long colisId,
        String usernameOperateur,
        String nomOperateurComplete,
        TypeEvenement typeEvenement,
        ZoneLogistique zoneAssignee,
        String emplacementBac,
        String commentaire,
        LocalDateTime horodatage
) {}