-- ==============================================================================
-- 1. INSERTION DES UTILISATEURS DE TEST
-- Mot de passe générique stocké (en production, utiliser BCryptPasswordEncoder)
-- ==============================================================================

INSERT INTO utilisateurs (id, username, password, nom, prenom, role, actif, created_at) VALUES
                                                                                            (1, 'admin', 'password123', 'DUPONT', 'Jean', 'ROLE_ADMIN', true, NOW()),
                                                                                            (2, 'superviseur_01', 'password123', 'MARTIN', 'Sophie', 'ROLE_SUPERVISOR', true, NOW()),
                                                                                            (3, 'OP_MARSEILLE_01', 'password123', 'BENALI', 'Karim', 'ROLE_OPERATOR', true, NOW()),
                                                                                            (4, 'OP_PARIS_01', 'password123', 'LEROY', 'Thomas', 'ROLE_OPERATOR', true, NOW()),
                                                                                            (5, 'OP_LYON_01', 'password123', 'MOREAU', 'Claire', 'ROLE_OPERATOR', true, NOW());


-- ==============================================================================
-- 2. INSERTION DES COLIS DE DÉMONSTRATION
-- ==============================================================================

-- Colis 1 : Destiné à Toulouse (Occitanie Ouest -> ZONE_12)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (1, 'FR31000897654', '31000', 'FR', 2.50, false, 'EN_TRI', 'ZONE_12_OCCITANIE_OUEST', NULL, NOW(), NOW());

-- Colis 2 : Destiné à Paris (IDF Nord -> ZONE_01)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (2, 'FR75011123456', '75011', 'FR', 1.20, false, 'EN_TRI', 'ZONE_01_IDF_NORD', NULL, NOW(), NOW());

-- Colis 3 : Destiné à Lyon (Auvergne-Rhône-Alpes -> ZONE_09)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (3, 'FR69002987654', '69002', 'FR', 5.80, false, 'EN_TRI', 'ZONE_09_AURA', NULL, NOW(), NOW());

-- Colis 4 : Destiné à la Belgique (International -> ZONE_16_ETRANGER)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (4, 'BE10009988776', '1000', 'BE', 0.85, false, 'EN_TRI', 'ZONE_16_ETRANGER', NULL, NOW(), NOW());

-- Colis 5 : Colis abîmé au déchargement (Zone de Rejet -> ZONE_99_REJET)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (5, 'FR13001001122', '13001', 'FR', 3.10, true, 'EN_REJET', 'ZONE_99_REJET', 'COLIS_ENDOMMAGE', NOW(), NOW());

-- Colis 6 : Code postal invalide / illisible (Zone de Rejet -> ZONE_99_REJET)
INSERT INTO colis (id, tracking_number, code_postal_destinataire, code_pays, poids_kg, est_endommage, statut, zone_destination, motif_rejet, created_at, updated_at) VALUES
    (6, 'FR99999000000', '99ABC', 'FR', 1.50, false, 'EN_REJET', 'ZONE_99_REJET', 'CODE_POSTAL_INVALIDE', NOW(), NOW());


-- ==============================================================================
-- 3. INSERTION DU HISTORIQUE DE TRAÇABILITÉ (MOUVEMENTS)
-- ==============================================================================

-- Mouvements pour le Colis #1 (Toulouse)
INSERT INTO mouvements (colis_id, operateur_id, type_evenement, zone_assignee, emplacement_bac, commentaire, horodatage) VALUES
    (1, 3, 'ORIENTE_TRI', 'ZONE_12_OCCITANIE_OUEST', 'BAC_TLS_01', 'Scan automatique quai A', NOW());

-- Mouvements pour le Colis #2 (Paris)
INSERT INTO mouvements (colis_id, operateur_id, type_evenement, zone_assignee, emplacement_bac, commentaire, horodatage) VALUES
    (2, 4, 'ORIENTE_TRI', 'ZONE_01_IDF_NORD', 'BAC_IDF_03', 'Prise en charge récepteur', NOW());

-- Mouvements pour le Colis #5 (Rejet physique)
INSERT INTO mouvements (colis_id, operateur_id, type_evenement, zone_assignee, emplacement_bac, commentaire, horodatage) VALUES
    (5, 3, 'SIGNALEMENT_ANOMALIE', 'ZONE_99_REJET', 'CHARIOT_REJET_R1', 'Carton écrasé sur le quai d arrivée', NOW());