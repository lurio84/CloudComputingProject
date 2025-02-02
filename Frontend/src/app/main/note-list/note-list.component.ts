import {Component, OnInit} from '@angular/core';
import {CreateNoteModalComponent} from "../create-note-modal/create-note-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-note-list',
  templateUrl: './note-list.component.html',
  styleUrls: ['./note-list.component.scss']
})
export class NoteListComponent implements OnInit{
  noteList: any[] = [];

  userId!: number;
  constructor(private dialog: MatDialog, private router: Router, private authService: AuthService) {
  }

  ngOnInit() {
    this.userId = this.authService.getUserId()!;
    this.getNoteInfo();
  }


  getNoteInfo(){
    this.authService.getNoteList(this.userId).subscribe((response)=> {
      this.noteList = response;
      console.log(this.noteList)
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

  share(e: Event) {
    e.preventDefault();
  }

  openNote(note: any) {
    this.router.navigate([`/note/${note.id}`], {queryParams: {user:  this.userId, noteId: note.id}});
  }
}
