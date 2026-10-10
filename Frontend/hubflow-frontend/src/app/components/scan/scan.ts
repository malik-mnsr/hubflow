import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

export interface ScanResponse {
  trackingNumber: string;
  zoneDestination: string;
  estRejet: boolean;
  motifRejet?: string;
  timestamp: string;
}

@Component({
  selector: 'app-scan',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './scan.html',
  styleUrls: ['./scan.css']
})
export class ScanComponent {
  private apiUrl = 'http://localhost:8080/api/v1/wms';

  trackingInput = '';
  poidsInput: number | null = null;
  codePostalInput = '';
  codePaysInput = 'FR';
  estEndommageInput = false; // 👈 Nouveau champ booléen (par défaut à false)

  dernierScan: ScanResponse | null = null;
  historiqueScans: ScanResponse[] = [];
  chargement = false;
  erreurMessage = '';

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  soumettreScan(): void {
    if (!this.trackingInput.trim()) return;

    this.chargement = true;
    this.erreurMessage = '';

    const payload = {
      trackingNumber: this.trackingInput.trim(),
      poidsKg: this.poidsInput,
      codePostalDestinataire: this.codePostalInput.trim(),
      codePays: this.codePaysInput.trim(),
      estEndommage: this.estEndommageInput // 👈 Toujours un boolean true ou false
    };

    this.http.post<ScanResponse>(`${this.apiUrl}/scan`, payload).subscribe({
      next: (response) => {
        this.dernierScan = response;
        this.historiqueScans.unshift(response);
        this.reinitialiserFormulaire();
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du scan :', err);
        this.erreurMessage = 'Échec de l\'envoi du scan. Vérifiez la connexion au backend.';
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  private reinitialiserFormulaire(): void {
    this.trackingInput = '';
    this.poidsInput = null;
    this.codePostalInput = '';
    this.codePaysInput = 'FR';
    this.estEndommageInput = false; // 👈 Remise à zéro
  }
}