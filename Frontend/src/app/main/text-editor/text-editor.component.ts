import { Component, OnDestroy, OnInit } from '@angular/core';
import { Editor } from 'ngx-editor';
import { WebSocketService } from "../../services/web-socket.service";
import { ActivatedRoute, Router } from "@angular/router";
import { NoteService } from "../note.service";
import { AuthService } from "../../services/auth.service";
import { noop, Subscription } from "rxjs";

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
  private webSocketSubscription!: Subscription;

  noteDetails: any;

  constructor(private webSocketService: WebSocketService,
              private route: ActivatedRoute,
              private noteService: NoteService,
              private authService: AuthService,
              private router: Router) {
    this.noteId = this.route.snapshot.params['id'];
  }

  ngOnInit(): void {
    this.userId = Number(this.authService.getUserId());
    this.editor = new Editor();

    this.authService.getNoteList(this.userId).subscribe(res => {
      const exist = res.some((note: any) => note.id === this.noteId);  // ✅ Fixed incorrect `=` operator
      if (!exist) {
        this.assignUser();
      }
    });

    // ✅ Connect to WebSocket and store the subscription
    this.webSocketSubscription = this.webSocketService.connect('http://localhost:8080/ws', this.noteId)
      .subscribe((message: any) => {
        console.log("📩 Message received:", message);

        if (message.noteId === this.noteId && message.content !== this.content) {
          this.content = message.content;
        }
      });

    // ✅ Fetch note details correctly
    this.noteService.get(this.noteId);
    this.noteService.getNotesDetail().subscribe((res: any) => {
      this.noteDetails = res;
      this.content = res.content;
    });
  }

  assignUser() {
    this.noteService.assignUser(this.noteId, this.userId).subscribe(noop);
  }

  /**
   * Called whenever the content of the editor changes.
   */
  onContentChange(newContent: string): void {
    clearTimeout(this.debounceTimer);

    this.debounceTimer = setTimeout(() => {
      const message = {
        noteId: this.noteId,
        userId: this.userId,
        content: newContent,
      };
      this.webSocketService.send(message); // ✅ Send the updated content via WebSocket
    }, 300); // ✅ Debounce time to reduce excessive WebSocket messages
  }

  ngOnDestroy(): void {
    this.editor?.destroy();

    if (this.webSocketSubscription) {
      this.webSocketSubscription.unsubscribe(); // ✅ Unsubscribe from WebSocket
    }

    this.webSocketService.close(); // ✅ Close WebSocket connection
  }

  back() {
    this.router.navigate(['/']);
  }
}
