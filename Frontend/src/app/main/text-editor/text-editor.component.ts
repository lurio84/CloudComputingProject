import { Component, OnDestroy, OnInit } from '@angular/core';
import { Editor } from 'ngx-editor';
import {WebSocketService} from "../../services/web-socket.service";


@Component({
  selector: 'app-text-editor',
  templateUrl: './text-editor.component.html',
  styleUrls: ['./text-editor.component.scss'],
})
export class TextEditorComponent implements OnInit, OnDestroy {
  editor!: Editor;
  content: string = '<p>Start typing here...</p>';
  private debounceTimer: any;

  constructor(private webSocketService: WebSocketService) {}

  ngOnInit(): void {
    this.editor = new Editor();

    // Connect to WebSocket and listen for updates
    this.webSocketService.connect('ws://localhost:8080/ws').subscribe((message: any) => {
      console.log(message)
      if (message.content !== this.content) {
        this.content = message.content;
      }
    });
  }

  /**
   * Called whenever the content of the editor changes.
   */
  onContentChange(newContent: string): void {
    clearTimeout(this.debounceTimer);

    this.debounceTimer = setTimeout(() => {
      const message = {
        noteId: 1, // Example note ID
        userId: 1, // Example user ID
        content: newContent,
      };
      this.webSocketService.send(message); // Send the updated content via WebSocket
    }, 300); // Debounce time to reduce excessive WebSocket messages
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
    this.webSocketService.close();
  }
}
