-- Nettoyage si réinitialisation
DROP TABLE IF EXISTS mouvements;
DROP TABLE IF EXISTS colis;
DROP TABLE IF EXISTS utilisateurs;

-- 1. Table Utilisateurs (Opérateurs & Superviseurs)
CREATE TABLE utilisateurs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              username VARCHAR(50) NOT NULL UNIQUE,
                              password VARCHAR(255) NOT NULL,
                              nom VARCHAR(100) NOT NULL,
                              prenom VARCHAR(100) NOT NULL,
                              role VARCHAR(30) NOT NULL,
                              actif BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table Colis
CREATE TABLE colis (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       tracking_number VARCHAR(64) NOT NULL UNIQUE,
                       code_postal_destinataire VARCHAR(10),
                       code_pays VARCHAR(3) NOT NULL DEFAULT 'FR',
                       poids_kg DOUBLE,
                       est_endommage BOOLEAN NOT NULL DEFAULT FALSE,
                       statut VARCHAR(30) NOT NULL,
                       zone_destination VARCHAR(40),
                       motif_rejet VARCHAR(255),
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       INDEX idx_colis_tracking (tracking_number),
                       INDEX idx_colis_zone (zone_destination),
                       INDEX idx_colis_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table Mouvements (Traçabilité & Historique de Scans)
CREATE TABLE mouvements (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            colis_id BIGINT NOT NULL,
                            operateur_id BIGINT NOT NULL,
                            type_evenement VARCHAR(30) NOT NULL,
                            zone_assignee VARCHAR(40),
                            emplacement_bac VARCHAR(50),
                            commentaire VARCHAR(255),
                            horodatage DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_mouvements_colis FOREIGN KEY (colis_id) REFERENCES colis(id) ON DELETE CASCADE,
                            CONSTRAINT fk_mouvements_operateur FOREIGN KEY (operateur_id) REFERENCES utilisateurs(id),

                            INDEX idx_mouvements_colis (colis_id),
                            INDEX idx_mouvements_operateur (operateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;