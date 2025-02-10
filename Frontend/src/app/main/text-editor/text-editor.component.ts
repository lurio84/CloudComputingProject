import { Component, OnDestroy, OnInit } from '@angular/core';
import { Editor } from 'ngx-editor';
import { WebSocketService } from "../../services/web-socket.service";
import { ActivatedRoute, Router } from "@angular/router";
import { NoteService } from "../note.service";
import { AuthService } from "../../services/auth.service";
import { noop, Subscription } from "rxjs";
import { diff_match_patch } from 'diff-match-patch';
import {environment} from "../../../environments/environments";


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
  private originalContent: string = "";
  noteDetails: any;

  isTyping = false;

  webSocketUrl = environment.websocketUrl

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
      const exist = res.some((note: any) => note.id === this.noteId);
      if (!exist) {
        this.assignUser();
      }
    });

    // ✅ Connect to WebSocket and subscribe
    this.webSocketSubscription = this.webSocketService.connect(this.webSocketUrl, this.noteId)
      .subscribe((message: any) => {
        if (message.noteId == this.noteId && message.userId != this.userId) {
          this.applyReceivedDiff(message.diff);
        } else {
          console.log("Skipping diff application for local user.");
        }
      });

    // ✅ Fetch initial content
    this.noteService.get(this.noteId);
    this.noteService.getNotesDetail().subscribe((res: any) => {
      this.noteDetails = res;
      this.content = res.content;
      this.originalContent = res.content; // Store original content for diff comparison
    });
  }

  assignUser() {
    this.noteService.assignUser(this.noteId, this.userId).subscribe(noop);
  }

  /**
   * Called whenever the content of the editor changes.
   * Computes the diff and sends it to WebSocket.
   */
  onContentChange(newContent: string): void {
    this.isTyping = true;
    clearTimeout(this.debounceTimer);

    this.debounceTimer = setTimeout(() => {
      this.isTyping = false;
      const dmp = new diff_match_patch();
      const diffs = dmp.diff_main(this.originalContent, newContent);
      dmp.diff_cleanupSemantic(diffs);
      const patch = dmp.patch_toText(dmp.patch_make(diffs));

      const message = {
        noteId: this.noteId,
        userId: this.userId,
        diff: patch
      };

      this.webSocketService.send(message);
      this.originalContent = newContent; // ✅ Update original content after sending
    }, 1); // Debounce time
  }

  /**
   * Applies received diffs to the current content.
   */
  applyReceivedDiff(diff: string): void {
    const dmp = new diff_match_patch();

    // Save the current cursor position
    const textarea = document.getElementById("noteContent") as HTMLTextAreaElement;
    const cursorPosition = textarea?.selectionStart || 0;

    const patches = dmp.patch_fromText(diff);
    const [updatedContent, _] = dmp.patch_apply(patches, this.content);

    if (this.content !== updatedContent) {
      this.content = updatedContent;
      this.originalContent = updatedContent;

      // ✅ Restore cursor position
      if (textarea) {
        textarea.setSelectionRange(cursorPosition, cursorPosition);
      }
    }
  }

  ngOnDestroy(): void {
    this.editor?.destroy();

    if (this.webSocketSubscription) {
      this.webSocketSubscription.unsubscribe();
    }

    this.webSocketService.close();
  }

  back() {
    this.router.navigate(['/']);
  }
}
