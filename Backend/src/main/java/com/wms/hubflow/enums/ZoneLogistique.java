package com.wms.hubflow.enums;

import lombok.Getter;

@Getter
public enum ZoneLogistique {
    ZONE_01_IDF_NORD("Île-de-France Nord", "HUB-IDF-N"),
    ZONE_02_IDF_SUD("Île-de-France Sud", "HUB-IDF-S"),
    ZONE_03_NORD("Nord-Pas-de-Calais", "HUB-LIL"),
    ZONE_04_GRAND_EST("Grand Est", "HUB-SXB"),
    ZONE_05_NORMANDIE("Normandie", "HUB-URO"),
    ZONE_06_BRETAGNE("Bretagne", "HUB-RNS"),
    ZONE_07_PAYS_DE_LOIRE("Pays de la Loire / Centre", "HUB-NTE"),
    ZONE_08_BOURGOGNE("Bourgogne-Franche-Comté", "HUB-DLE"),
    ZONE_09_AURA("Auvergne-Rhône-Alpes", "HUB-LYS"),
    ZONE_10_NOUVELLE_AQUITAINE_NORD("Nouvelle-Aquitaine Nord", "HUB-BOD"),
    ZONE_11_NOUVELLE_AQUITAINE_SUD("Nouvelle-Aquitaine Sud", "HUB-PUF"),
    ZONE_12_OCCITANIE_OUEST("Occitanie Ouest", "HUB-TLS"),
    ZONE_13_OCCITANIE_EST("Occitanie Est", "HUB-MPL"),
    ZONE_14_PACA_CORSE("PACA & Corse", "HUB-MRS"),
    ZONE_15_MASSIF_CENTRAL("Massif Central", "HUB-CFE"),
    ZONE_16_ETRANGER("International & DOM-TOM", "HUB-INT"),
    ZONE_99_REJET("Zone de Rejet / Anomalie", "HUB-REJ");

    private final String libelle;
    private final String codeHub;

    ZoneLogistique(String libelle, String codeHub) {
        this.libelle = libelle;
        this.codeHub = codeHub;
    }
}