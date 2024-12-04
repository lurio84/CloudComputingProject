import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
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



@NgModule({
  declarations: [
    MainComponent,
    ToolBarComponent,
    TextEditorComponent
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
    ReactiveFormsModule
  ]
})
export class MainModule { }
