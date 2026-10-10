package com.wms.hubflow.dto;

import com.wms.hubflow.enums.ZoneLogistique;
import lombok.Builder;

@Builder
public record ZoneStatDto(
        ZoneLogistique zone,
        String libelleZone,
        String codeHub,
        long totalColis,
        boolean estAlerte
) {}