package com.wms.hubflow.dto;

import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.ZoneLogistique;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ColisDto(
        Long id,
        String trackingNumber,
        String codePostalDestinataire,
        String codePays,
        Double poidsKg,
        boolean estEndommage,
        StatutColis statut,
        ZoneLogistique zoneDestination,
        String motifRejet,
        List<MouvementDto> mouvements,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}