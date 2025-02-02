import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NoteService} from "../note.service";
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-create-note-modal',
  templateUrl: './create-note-modal.component.html',
  styleUrls: ['./create-note-modal.component.scss']
})
export class CreateNoteModalComponent implements OnInit{
  form!: FormGroup;
  userId!: number;
  constructor(    public dialogRef: MatDialogRef<CreateNoteModalComponent>,
                  private fb: FormBuilder,
                  private noteService: NoteService,
                  private AuthService: AuthService,
                 private router: Router,

                  @Inject(MAT_DIALOG_DATA) public data: any,) {
  }

  ngOnInit() {
    this.createForm();
    this.userId = Number(this.AuthService.getUserId())

  }

  closeDialog(): void {
    this.dialogRef.close(); // ✅ Close the dialog
  }

  createForm(){
    this.form = this.fb.group({
      title: [''],
      content: [''],

    })
  }

  submitForm() {
    const form = {...this.form.value, userId: this.userId};
    this.noteService.createNote(form).subscribe(note=> {
      this.dialogRef.close();
      this.router.navigate([`/note/${note.id}`]);
    })

  }
}
