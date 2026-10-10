import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private stompClient!: Client;
  private scanSubject = new Subject<any>();

  constructor() {
    this.initWebSocket();
  }

  private initWebSocket(): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-wms'),
      reconnectDelay: 5000,
      debug: (str) => console.log(str)
    });

    this.stompClient.onConnect = () => {
      console.log('Connecté au WebSocket WMS');
      
      this.stompClient.subscribe('/topic/scans', (message) => {
        if (message.body) {
          const scanResult = JSON.parse(message.body);
          this.scanSubject.next(scanResult);
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Erreur STOMP :', frame.headers['message']);
    };

    this.stompClient.activate();
  }

  public ecouterScans(): Observable<any> {
    return this.scanSubject.asObservable();
  }
}