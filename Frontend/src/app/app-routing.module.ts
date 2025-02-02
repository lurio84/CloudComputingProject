import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AuthorizeComponent} from "./components/authorize/authorize.component";
import {AuthGuard} from "./gurad/auth";
import {LoginGuard} from "./gurad/login";

const routes: Routes = [
  {
    path: 'login', component: AuthorizeComponent,
    canActivate: [LoginGuard]
  },
  {
    path: '',
    loadChildren: () => import('./main/main.module').then((m) => m.MainModule),
    canActivate: [AuthGuard]
  },
  {path: '**', redirectTo: 'home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
