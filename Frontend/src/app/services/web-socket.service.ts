import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private socket!: WebSocket;
  private messageSubject = new Subject<any>();

  connect(url: string): Observable<any> {
    this.socket = new WebSocket(url);

    this.socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.messageSubject.next(message);
    };

    this.socket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return this.messageSubject.asObservable();
  }

  send(message: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    }
  }

  close(): void {
    if (this.socket) {
      this.socket.close();
    }
  }
}
