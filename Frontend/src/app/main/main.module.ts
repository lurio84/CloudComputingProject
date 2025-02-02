import { NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';

import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main/main.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import { ToolBarComponent } from './tool-bar/tool-bar.component';
import {MatBadgeModule} from "@angular/material/badge";
import {TextEditorComponent} from "./text-editor/text-editor.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgxEditorModule} from "ngx-editor";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatCardModule} from "@angular/material/card";
import { CreateNoteModalComponent } from './create-note-modal/create-note-modal.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import { NoteListComponent } from './note-list/note-list.component';



@NgModule({
  declarations: [
    MainComponent,
    ToolBarComponent,
    TextEditorComponent,
    CreateNoteModalComponent,
    NoteListComponent
  ],
  imports: [
    CommonModule,
    MainRoutingModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    FormsModule,
    NgxEditorModule,
    ReactiveFormsModule,
    MatGridListModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
})
export class MainModule { }
