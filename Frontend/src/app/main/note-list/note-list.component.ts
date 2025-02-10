import {Component, HostListener, OnInit} from '@angular/core';
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
  gridCols: number = 4;
  userId!: number;
  constructor(private dialog: MatDialog,
              private router: Router,
              private noteService: NoteService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.userId = this.authService.getUserId()!;
    this.getNoteInfo();
    this.updateGridCols();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.updateGridCols();
  }

  updateGridCols() {
    const screenWidth = window.innerWidth;
    if (screenWidth < 600) {
      this.gridCols = 1;  // Mobile view (1 column)
    } else if (screenWidth < 960) {
      this.gridCols = 2;  // Tablet view (2 columns)
    } else if (screenWidth < 1280) {
      this.gridCols = 3;  // Small desktop (3 columns)
    } else {
      this.gridCols = 4;  // Large screens (4 columns)
    }
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
      data: `${window.location.origin}/note/${note.id}`

    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  openNote(note: any) {
    this.router.navigate([`/note/${note.id}`]);
  }
}
