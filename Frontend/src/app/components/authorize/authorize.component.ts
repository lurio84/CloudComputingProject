import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-authorize',
  templateUrl: './authorize.component.html',
  styleUrls: ['./authorize.component.scss']
})
export class AuthorizeComponent {
  userId = 1;


  constructor(private router: Router, private authService: AuthService) {}

  login(): void {
    this.authService.login(this.userId).subscribe(
      (response) => {
        if (response.success) {
          this.router.navigate(['/note-editor']); // Navigate to note editor after successful login
        }
      },
      (error) => {
        console.error('Login failed:', error);
        alert('Login failed. Please try again.');
      }
    );
  }
}
