package com.wms.hubflow.dto;

import com.wms.hubflow.enums.RoleUtilisateur;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UtilisateurDto(
        Long id,
        String username,
        String nom,
        String prenom,
        RoleUtilisateur role,
        boolean actif,
        LocalDateTime createdAt
) {}