import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {MainComponent} from "./main/main.component";
import {TextEditorComponent} from "./text-editor/text-editor.component";
import {NoteListComponent} from "./note-list/note-list.component";

const routes: Routes = [
  {path: '', component: MainComponent , children:[
      {path:'', component: NoteListComponent},
      {path: 'note/:id', component: TextEditorComponent}
    ]},

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
