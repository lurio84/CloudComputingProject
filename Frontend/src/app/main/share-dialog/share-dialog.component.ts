import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-share-dialog',
  templateUrl: './share-dialog.component.html',
  styleUrls: ['./share-dialog.component.scss']
})
export class ShareDialogComponent {
constructor(  public dialogRef: MatDialogRef<ShareDialogComponent>,  @Inject(MAT_DIALOG_DATA) public data: any) {
}

  click() {
    this.dialogRef.close();
  }
}
