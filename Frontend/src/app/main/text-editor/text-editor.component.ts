import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";
import {Editor, Toolbar} from "ngx-editor";


@Component({
  selector: 'app-text-editor',
  templateUrl: './text-editor.component.html',
  styleUrls: ['./text-editor.component.scss'],
})
export class TextEditorComponent implements OnInit, OnDestroy{
  editor!: Editor;
  content: string = '<p>Start typing here...</p>';


  constructor() {
  }

  ngOnInit(): void {
    this.editor = new Editor();

  }

  // make sure to destory the editor


  ngOnDestroy(): void {
    this.editor?.destroy();
  }
}
