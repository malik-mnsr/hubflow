package com.wms.hubflow.dto;

import com.wms.hubflow.enums.StatutColis;
import com.wms.hubflow.enums.ZoneLogistique;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ScanResultDto(
        String trackingNumber,
        ZoneLogistique zoneDestination,
        String libelleZone,
        String codeHub,
        StatutColis statut,
        boolean estRejet,
        String motifRejet,
        LocalDateTime horodatage
) {}