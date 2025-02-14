import { Component, OnDestroy, OnInit, ElementRef, ViewChild } from '@angular/core';
import { Editor } from 'ngx-editor';
import { WebSocketService } from "../../services/web-socket.service";
import { ActivatedRoute, Router } from "@angular/router";
import { NoteService } from "../note.service";
import { AuthService } from "../../services/auth.service";
import { noop, Subscription } from "rxjs";
import { diff_match_patch } from 'diff-match-patch';
import { environment } from "../../../environments/environments";

@Component({
  selector: 'app-text-editor',
  templateUrl: './text-editor.component.html',
  styleUrls: ['./text-editor.component.scss'],
})
export class TextEditorComponent implements OnInit, OnDestroy {
  editor!: Editor;
  content: string = '';
  private debounceTimer: any;
  userId!: number;
  noteId!: number;
  private webSocketSubscription!: Subscription;
  private originalContent: string = "";
  noteDetails: any;

  webSocketUrl = environment.websocketUrl;
  isTyping = false;

  @ViewChild('noteContent') noteContent!: ElementRef<HTMLTextAreaElement>; // Reference to textarea

  constructor(
    private webSocketService: WebSocketService,
    private route: ActivatedRoute,
    private noteService: NoteService,
    private authService: AuthService,
    private router: Router
  ) {
    this.noteId = this.route.snapshot.params['id'];
  }

  ngOnInit(): void {
    this.userId = Number(this.authService.getUserId());
    this.editor = new Editor();

    // Check if user has access to the note
    this.authService.getNoteList(this.userId).subscribe(res => {
      const exist = res.some((note: any) => note.id === this.noteId);
      if (!exist) {
        this.assignUser();
      }
    });

    // ✅ WebSocket Connection
    this.webSocketSubscription = this.webSocketService.connect(this.webSocketUrl, this.noteId)
      .subscribe((message: any) => {
        if (message.userId === this.userId) return;
        if (message.noteId == this.noteId && message.userId != this.userId) {
          this.applyReceivedDiff(message.diff);
        }
      });

    // ✅ Fetch initial content
    this.noteService.get(this.noteId);
    this.noteService.getNotesDetail().subscribe((res: any) => {
      this.noteDetails = res;
      this.content = res.content;
      this.originalContent = res.content; // Store original content
    });
  }

  assignUser() {
    this.noteService.assignUser(this.noteId, this.userId).subscribe(noop);
  }

  /**
   * Called when user types. Sends diff to WebSocket.
   */
  onContentChange(newContent: string): void {
    const textarea = this.noteContent?.nativeElement;
    if (!textarea) return;

    const cursorPosition = textarea.selectionStart; // ✅ Save local cursor before sending

    const dmp = new diff_match_patch();
    const diffs = dmp.diff_main(this.originalContent, newContent);
    dmp.diff_cleanupSemantic(diffs);
    const patch = dmp.patch_toText(dmp.patch_make(diffs));

    const message = {
      noteId: this.noteId,
      userId: this.userId,
      diff: patch
    };

    this.webSocketService.send(message); // ✅ Send instantly
    this.originalContent = newContent; // ✅ Update local state

    // ✅ Restore local cursor after update
    setTimeout(() => {
      textarea.setSelectionRange(cursorPosition, cursorPosition);
    }, 0);
  }


  /**
   * Applies received diff updates while preserving cursor position.
   */
  applyReceivedDiff(diff: string): void {
    const textarea = this.noteContent?.nativeElement;
    if (!textarea) return;

    const cursorPosition = textarea.selectionStart; // ✅ Save cursor before applying diff

    const dmp = new diff_match_patch();
    const patches = dmp.patch_fromText(diff);
    const [updatedContent, results] = dmp.patch_apply(patches, this.content);

    if (results.some((applied) => applied)) { // ✅ Apply only if patch was applied
      this.content = updatedContent;
      this.originalContent = updatedContent;

      // ✅ Restore cursor without disrupting typing
      setTimeout(() => {
        textarea.setSelectionRange(cursorPosition, cursorPosition);
      }, 0);
    }
  }



  ngOnDestroy(): void {
    this.editor?.destroy();
    this.webSocketSubscription?.unsubscribe();
    this.webSocketService.close();
  }

  back() {
    this.router.navigate(['/']);
  }
}
