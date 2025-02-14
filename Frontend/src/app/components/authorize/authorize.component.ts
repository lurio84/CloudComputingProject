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
  formLogin!: FormGroup;


  constructor(private router: Router, private authService: AuthService, private fb: FormBuilder) {}

  ngOnInit() {
    this.createForm();
    this.createFormB()
  }

  createForm() {
    this.form = this.fb.group({
      username: [''],
      email: [''],
      password: [''],
    });
  }

  createFormB() {
    this.formLogin = this.fb.group({
      username: [''],
      password: [''],
    });
  }


  register(): void {
    this.authService.register(this.form.value).subscribe(
      (response) => {
        if (response) {
          this.router.navigate(['/']); // Navigate to note editor after successful login
        }
      },
      (error) => {
        alert(error.error.message);
      }
    );
  }

  login() {
    this.authService.login(this.formLogin.value).subscribe(
      (response) => {
        if (response) {
          this.router.navigate(['/']); // Navigate to note editor after successful login
        }
      },
      (error) => {
        console.error('Login failed:', error.error.message);
        alert(error.error.message);
      }
    );
  }
}
