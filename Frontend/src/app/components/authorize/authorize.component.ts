import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-authorize',
  templateUrl: './authorize.component.html',
  styleUrls: ['./authorize.component.scss']
})
export class AuthorizeComponent implements OnInit{
  userId = 1;
  form!: FormGroup;


  constructor(private router: Router, private authService: AuthService, private fb: FormBuilder) {}

  ngOnInit() {
    this.createForm()
  }

  createForm() {
    this.form = this.fb.group({
      username: ['tes@tes'],
      email: ['tes@tes'],
      password: [12345678],
    });
  }

  login(): void {
    this.authService.login(this.form.value).subscribe(
      (response) => {
        // if (response) {
        //   this.router.navigate(['/home']); // Navigate to note editor after successful login
        // }
      },
      (error) => {
        console.error('Login failed:', error);
        alert('Login failed. Please try again.');
      }
    );
  }
}
