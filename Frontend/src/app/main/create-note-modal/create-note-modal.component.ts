import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-create-note-modal',
  templateUrl: './create-note-modal.component.html',
  styleUrls: ['./create-note-modal.component.scss']
})
export class CreateNoteModalComponent implements OnInit{
  form!: FormGroup
  // readonly dialogRef = inject(MatDialogRef<CreateNoteModalComponent>);

  // onNoClick(): void {
  //   this.dialogRef.close();
  // }
  constructor(    public dialogRef: MatDialogRef<CreateNoteModalComponent>,
                  private fb: FormBuilder,


                  @Inject(MAT_DIALOG_DATA) public data: any,) {
  }

  ngOnInit() {
    this.createForm();
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

  }
}
