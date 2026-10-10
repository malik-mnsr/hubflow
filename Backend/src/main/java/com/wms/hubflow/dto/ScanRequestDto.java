package com.wms.hubflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ScanRequestDto(
        @NotBlank(message = "Le numéro de tracking est obligatoire")
        String trackingNumber,

        String codePostalDestinataire,
        String codePays,
        Double poidsKg,
        boolean estEndommage,

        @NotBlank(message = "L'identifiant de l'opérateur est obligatoire")
        String usernameOperateur,

        String emplacementBac,
        String commentaire
) {}