import {Component, OnInit} from '@angular/core';
import {CreateNoteModalComponent} from "../create-note-modal/create-note-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {NoteService} from "../note.service";
import {ShareDialogComponent} from "../share-dialog/share-dialog.component";
import {noop} from "rxjs";

@Component({
  selector: 'app-note-list',
  templateUrl: './note-list.component.html',
  styleUrls: ['./note-list.component.scss']
})
export class NoteListComponent implements OnInit{
  noteList: any[] = [];

  userId!: number;
  constructor(private dialog: MatDialog,
              private router: Router,
              private noteService: NoteService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.userId = this.authService.getUserId()!;
    this.getNoteInfo();
  }


  getNoteInfo(){
    this.authService.getNoteList(this.userId).subscribe((response)=> {
      this.noteList = response;
    })
  }
  createNote() {
    const dialogRef = this.dialog.open(CreateNoteModalComponent, {
      width: '500px',

    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  share(e: Event, note: any) {
    e.stopPropagation();
    this.noteService.shareNote(note?.id).subscribe(noop);
    const dialogRef = this.dialog.open(ShareDialogComponent, {
      width: '400px',
      data: `http://localhost:4200/note/${note.id}`

    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  openNote(note: any) {
    this.router.navigate([`/note/${note.id}`]);
  }
}
