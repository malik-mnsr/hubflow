export interface ZoneStat {
  zone: string;
  libelleZone: string;
  codeHub: string;
  totalColis: number;
  estAlerte?: boolean;
}

export interface Colis {
  id: number;
  trackingNumber: string;
  codePostalDestinataire: string;
  codePays: string;
  poidsKg: number;
  estEndommage: boolean;
  statut: string;
  zoneDestination: string;
  motifRejet?: string;
  updatedAt: string;
}