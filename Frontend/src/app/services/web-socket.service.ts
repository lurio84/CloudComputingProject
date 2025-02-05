import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client } from "@stomp/stompjs";
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private messageSubject = new Subject<any>();

  connect(url: string, noteId: number): Observable<any> {
    return new Observable(observer => {
      const socket = new SockJS(url); // ✅ Create a SockJS instance

      this.stompClient = new Client({
        webSocketFactory: () => socket, // ✅ Use SockJS as WebSocket
        reconnectDelay: 5000, // ✅ Auto-reconnect every 5 seconds
        debug: (msg) => console.log(msg), // ✅ Debug messages
      });

      // ✅ Define connection behavior
      this.stompClient.onConnect = () => {
        console.log('✅ WebSocket Connected to:', url);

        // ✅ Subscribe to the correct topic for this note
        const topic = `/topic/notes/
        {noteId}`;
        this.stompClient!.subscribe(topic, (message) => {
          const parsedMessage = JSON.parse(message.body);
          observer.next(parsedMessage);
        });
      };

      this.stompClient.activate(); // ✅ Start the connection

      // ✅ Handle errors
      this.stompClient.onStompError = (error) => {
        console.error("❌ STOMP error:", error);
      };
    });
  }

  send(message: any): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: "/app/edit",
        body: JSON.stringify(message),
      });
    } else {
      console.error("❌ WebSocket is not connected. Message not sent.");
    }
  }

  close(): void {
    if (this.stompClient) {
      this.stompClient.deactivate(); // ✅ Use deactivate() instead of disconnect()
      console.log("✅ WebSocket Disconnected");
    }
  }
}
