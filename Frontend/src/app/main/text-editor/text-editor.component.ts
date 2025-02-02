import { Component, OnDestroy, OnInit } from '@angular/core';
import { Editor } from 'ngx-editor';
import {WebSocketService} from "../../services/web-socket.service";
import {ActivatedRoute, Router} from "@angular/router";
import {NoteService} from "../note.service";


@Component({
  selector: 'app-text-editor',
  templateUrl: './text-editor.component.html',
  styleUrls: ['./text-editor.component.scss'],
})
export class TextEditorComponent implements OnInit, OnDestroy {
  editor!: Editor;
  content: string = '<p></p>';
  private debounceTimer: any;
  userId!: number;
  noteId!: number;

  noteDetails: any ;

  constructor(private webSocketService: WebSocketService,
              private route:ActivatedRoute,
              private noteService: NoteService,
              private router: Router) {
    this.userId = this.route.snapshot.queryParams['user'];
    this.noteId = this.route.snapshot.queryParams['noteId'];


  }

  ngOnInit(): void {
    this.editor = new Editor();

    // Connect to WebSocket and listen for updates
    this.webSocketService.connect('ws://localhost:8080/ws').subscribe((message: any) => {
      console.log(message)
      if (message.content !== this.content) {
        this.content = message.content;
      }
    });
    this.noteService.get(this.noteId);
    this.noteService.getNotesDetail().subscribe((res:any)=> {
      this.noteDetails = res;
      this.content = res.content;

    })
  }

  /**
   * Called whenever the content of the editor changes.
   */
  onContentChange(newContent: string): void {
    clearTimeout(this.debounceTimer);

    this.debounceTimer = setTimeout(() => {
      const message = {
        noteId: this.noteId, // Example note ID
        userId: this.userId, // Example user ID
        content: newContent,
      };
      this.webSocketService.send(message); // Send the updated content via WebSocket
    }, 300); // Debounce time to reduce excessive WebSocket messages
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
    this.webSocketService.close();
  }

  back() {
    this.router.navigate(['/'])
  }
}
