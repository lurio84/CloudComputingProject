import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NoteService} from "../note.service";

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.scss']
})
export class ToolBarComponent implements OnInit{
@Input() user: any;
@Output() logoutEmitter: EventEmitter<any> = new EventEmitter<any>();
usersNumber: Number =0;

constructor(private noteService: NoteService) {
}

ngOnInit() {
  this.getUsersNumber();
}

  logout() {
    this.logoutEmitter.emit()
  }

  getUsersNumber(){
    this.noteService.getUsersInNOte().subscribe(res => {
      this.usersNumber = res;
    })

  }
}
