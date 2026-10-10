package com.wms.hubflow.controller;

import com.wms.hubflow.dto.ColisDto;
import com.wms.hubflow.dto.ScanRequestDto;
import com.wms.hubflow.dto.ScanResultDto;
import com.wms.hubflow.dto.ZoneStatDto;
import com.wms.hubflow.enums.ZoneLogistique;
import com.wms.hubflow.service.ColisService;
import com.wms.hubflow.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permet les requêtes depuis l'application mobile & Angular en dev
@Tag(name = "WMS Operations", description = "Endpoints pour les scans opérateurs, le suivi des 17 zones et la gestion des réjections")
public class ScanController {

    private final ScanService scanService;
    private final ColisService colisService;

    // --------------------------------------------------------------------------------
    // 1. ENDPOINT SCAN (Opérateur Mobile / Scanner)
    // --------------------------------------------------------------------------------

    @Operation(
            summary = "Traiter le scan d'un colis",
            description = "Reçoit un numéro de tracking et ses métadonnées, calcule la zone de destination (15 régionales, Étranger, Rejet), met à jour le statut et publie un événement WebSocket."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Scan traité avec succès et zone attribuée",
                    content = @Content(schema = @Schema(implementation = ScanResultDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Données du scan invalides"),
            @ApiResponse(responseCode = "404", description = "Opérateur introuvable")
    })
    @PostMapping("/scan")
    public ResponseEntity<ScanResultDto> effectuerScan(@Valid @RequestBody ScanRequestDto request) {
        ScanResultDto resultat = scanService.traiterScan(request);
        return ResponseEntity.ok(resultat);
    }

    // --------------------------------------------------------------------------------
    // 2. ENDPOINTS DASHBOARD & SUPERVISION (WebApp Angular)
    // --------------------------------------------------------------------------------

    @Operation(
            summary = "Récupérer les statistiques par zone logistique",
            description = "Retourne la liste complète des 17 zones avec leurs libellés, codes de Hubs et le volume actuel de colis pour le Dashboard."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Statistiques récupérées avec succès",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ZoneStatDto.class)))
    )
    @GetMapping("/dashboard/stats-zones")
    public ResponseEntity<List<ZoneStatDto>> obtenirStatistiquesParZone() {
        List<ZoneStatDto> stats = colisService.obtenirStatistiquesDashboard();
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "Lister les colis filtrés par zone",
            description = "Permet d'afficher la liste des colis stockés dans un Hub spécifique ou en zone de Rejet (ex: ZONE_99_REJET)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des colis retournée",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ColisDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Code de zone invalide")
    })
    @GetMapping("/colis/zone/{zone}")
    public ResponseEntity<List<ColisDto>> obtenirColisParZone(
            @Parameter(description = "Code de l'enum ZoneLogistique (ex: ZONE_01_IDF_NORD, ZONE_99_REJET)", example = "ZONE_99_REJET")
            @PathVariable ZoneLogistique zone) {

        List<ColisDto> colisEnZone = colisService.obtenirParZone(zone);
        return ResponseEntity.ok(colisEnZone);
    }

    @Operation(
            summary = "Obtenir la fiche complète d'un colis et sa traçabilité",
            description = "Retourne les détails d'un colis ainsi que tout l'historique de ses mouvements scannés."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Colis trouvé",
                    content = @Content(schema = @Schema(implementation = ColisDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Aucun colis trouvé avec ce numéro de tracking")
    })
    @GetMapping("/colis/{trackingNumber}")
    public ResponseEntity<ColisDto> obtenirFicheColis(
            @Parameter(description = "Numéro de suivi unique du colis", example = "FR31000897654")
            @PathVariable String trackingNumber) {

        ColisDto colis = colisService.obtenirParTrackingNumber(trackingNumber);
        return ResponseEntity.ok(colis);
    }

    // --------------------------------------------------------------------------------
    // 3. GESTION DES ANOMALIES (Superviseur)
    // --------------------------------------------------------------------------------

    @Operation(
            summary = "Corriger manuellement une anomalie / rejet",
            description = "Permet à un superviseur de mettre à jour le code postal d'un colis bloqué en ZONE_99_REJET et d'exécuter à nouveau le routage."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Anomalie corrigée et colis réorienté",
                    content = @Content(schema = @Schema(implementation = ColisDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Colis ou superviseur introuvable")
    })
    @PutMapping("/colis/{trackingNumber}/corriger-anomalie")
    public ResponseEntity<ColisDto> corrigerAnomalie(
            @Parameter(description = "Numéro de suivi du colis bloqué", example = "FR99999000000")
            @PathVariable String trackingNumber,
            @Parameter(description = "Nouveau code postal français valide (5 chiffres)", example = "31000")
            @RequestParam String nouveauCodePostal,
            @Parameter(description = "Username du superviseur effectuant la correction", example = "superviseur_01")
            @RequestParam String usernameSuperviseur) {

        ColisDto colisCorrige = colisService.corrigerAnomalie(trackingNumber, nouveauCodePostal, usernameSuperviseur);
        return ResponseEntity.ok(colisCorrige);
    }
}