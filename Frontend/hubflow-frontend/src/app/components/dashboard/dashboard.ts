import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ZoneStat, Colis } from '../../models/zone.model';
import { WebsocketService } from '../../services/websocket';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  
  private apiUrl = 'http://localhost:8080/api/v1/wms';
  private wsSubscription!: Subscription;

  zones: { code: string; libelle: string; codeHub: string; totalColis: number; estAlerte?: boolean; estSpecial?: boolean }[] = [
    { code: 'ZONE_01_IDF_NORD', libelle: 'Île-de-France Nord', codeHub: 'HUB-IDF-N', totalColis: 0 },
    { code: 'ZONE_02_IDF_SUD', libelle: 'Île-de-France Sud', codeHub: 'HUB-IDF-S', totalColis: 0 },
    { code: 'ZONE_03_NORD', libelle: 'Nord-Pas-de-Calais', codeHub: 'HUB-LIL', totalColis: 0 },
    { code: 'ZONE_04_GRAND_EST', libelle: 'Grand Est', codeHub: 'HUB-SXB', totalColis: 0 },
    { code: 'ZONE_05_NORMANDIE', libelle: 'Normandie', codeHub: 'HUB-URO', totalColis: 0 },
    { code: 'ZONE_06_BRETAGNE', libelle: 'Bretagne', codeHub: 'HUB-RNS', totalColis: 0 },
    { code: 'ZONE_07_PAYS_DE_LOIRE', libelle: 'Pays de la Loire / Centre', codeHub: 'HUB-NTE', totalColis: 0 },
    { code: 'ZONE_08_BOURGOGNE', libelle: 'Bourgogne-Franche-Comté', codeHub: 'HUB-DLE', totalColis: 0 },
    { code: 'ZONE_09_AURA', libelle: 'Auvergne-Rhône-Alpes', codeHub: 'HUB-LYS', totalColis: 0 },
    { code: 'ZONE_10_NOUVELLE_AQUITAINE_NORD', libelle: 'Nouvelle-Aquitaine Nord', codeHub: 'HUB-BOD', totalColis: 0 },
    { code: 'ZONE_11_NOUVELLE_AQUITAINE_SUD', libelle: 'Nouvelle-Aquitaine Sud', codeHub: 'HUB-PUF', totalColis: 0 },
    { code: 'ZONE_12_OCCITANIE_OUEST', libelle: 'Occitanie Ouest', codeHub: 'HUB-TLS', totalColis: 0 },
    { code: 'ZONE_13_OCCITANIE_EST', libelle: 'Occitanie Est', codeHub: 'HUB-MPL', totalColis: 0 },
    { code: 'ZONE_14_PACA_CORSE', libelle: 'PACA & Corse', codeHub: 'HUB-MRS', totalColis: 0 },
    { code: 'ZONE_15_MASSIF_CENTRAL', libelle: 'Massif Central', codeHub: 'HUB-CFE', totalColis: 0 },
    { code: 'ZONE_16_ETRANGER', libelle: 'International / Export', codeHub: 'HUB-INT', totalColis: 0, estSpecial: true },
    { code: 'ZONE_99_REJET', libelle: 'Zone de Rejet / Anomalies', codeHub: 'HUB-REJ', totalColis: 0, estAlerte: true }
  ];

  selectedZone: any = null;
  colisZoneSelectionnee: Colis[] = [];
  loadingColis = false;
  
  totalColisTraites = 0;
  totalRejets = 0;

  constructor(
    private http: HttpClient, 
    private wsService: WebsocketService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerStatistiques();

    if (this.wsService) {
      this.wsSubscription = this.wsService.ecouterScans().subscribe({
        next: (scanResult) => {
          if (scanResult) {
            this.incrementerZoneEnTempsReel(scanResult);
            this.cdr.detectChanges();
          }
        },
        error: (err) => console.error('Erreur flux WebSocket :', err)
      });
    }
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  chargerStatistiques(): void {
    this.http.get<ZoneStat[]>(`${this.apiUrl}/dashboard/stats-zones`).subscribe({
      next: (statsBackend) => {
        if (!Array.isArray(statsBackend)) return;

        let cumulTotal = 0;
        let cumulRejets = 0;

        statsBackend.forEach(stat => {
          if (!stat) return;
          const zoneObj = this.zones.find(z => z.code === stat.zone);
          if (zoneObj) {
            zoneObj.totalColis = stat.totalColis || 0;
          }
          cumulTotal += stat.totalColis || 0;
          if (stat.zone === 'ZONE_99_REJET') {
            cumulRejets = stat.totalColis || 0;
          }
        });

        this.totalColisTraites = cumulTotal;
        this.totalRejets = cumulRejets;
        this.cdr.detectChanges();
      },
      error: (err) => console.warn('Backend non disponible.', err)
    });
  }

  private incrementerZoneEnTempsReel(scanResult: any): void {
    const zoneTarget = this.zones.find(z => z.code === scanResult.zoneDestination);
    if (zoneTarget) {
      zoneTarget.totalColis++;
      this.totalColisTraites++;
      if (scanResult.estRejet || scanResult.zoneDestination === 'ZONE_99_REJET') {
        this.totalRejets++;
      }
    }
  }

  selectionnerZone(zone: any): void {
    this.selectedZone = zone;
    this.colisZoneSelectionnee = [];
    this.loadingColis = true;
    this.cdr.detectChanges();

    this.http.get<Colis[]>(`${this.apiUrl}/colis/zone/${zone.code}`).subscribe({
      next: (colisList) => {
        console.log(`Données reçues pour la zone ${zone.code} :`, colisList);
        this.colisZoneSelectionnee = Array.isArray(colisList) ? colisList : [];
        this.loadingColis = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(`Erreur lors de la récupération des colis pour ${zone.code}:`, err);
        this.colisZoneSelectionnee = [];
        this.loadingColis = false;
        this.cdr.detectChanges();
      }
    });
  }

  fermerModal(): void {
    this.selectedZone = null;
    this.colisZoneSelectionnee = [];
    this.cdr.detectChanges();
  }
}