import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="main-navbar">
      <div class="nav-brand">HubFlow WMS</div>
      <div class="nav-links">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard (Supervisor)</a>
        <a routerLink="/scan" routerLinkActive="active">Station de Scan (Operator)</a>
      </div>
    </nav>
    <main class="content-container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .main-navbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 2rem;
      background: #1e293b;
      color: #fff;
      border-bottom: 1px solid #334155;
    }
    .nav-brand { font-weight: bold; font-size: 1.2rem; color: #38bdf8; }
    .nav-links a {
      color: #94a3b8;
      text-decoration: none;
      margin-left: 1.5rem;
      padding: 0.4rem 0.8rem;
      border-radius: 4px;
      font-weight: 500;
    }
    .nav-links a.active {
      color: #fff;
      background: #334155;
    }
  `]
})
export class AppComponent {
  title = 'hubflow-frontend';
}