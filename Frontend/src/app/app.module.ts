import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {HttpClientModule} from "@angular/common/http";
import { AuthorizeComponent } from './components/authorize/authorize.component';
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {MatTabsModule} from "@angular/material/tabs";


@NgModule({
  declarations: [
    AppComponent,
    AuthorizeComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        FormsModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule,
        MatTabsModule,

    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
