import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import {HttpClient, HttpParams} from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private userId: number | null = null;
  baseURL = 'http://localhost:8080/';

  constructor(private http: HttpClient, private router: Router) {}

  /**
   * Login method to authenticate the user.
   */
  login(form: any): Observable<any> {
    let queryParams = new HttpParams();
    queryParams = queryParams.append('username', form.username);
    queryParams = queryParams.append('email', form.email)
    queryParams = queryParams.append('password', form.password)
    return this.http.post<any>(`${this.baseURL}users`,   '', {params: queryParams}).pipe(
      tap((response) => {
        console.log(response)
        if (response) {
          this.isAuthenticatedSubject.next(true);
          this.userId = response.id;
          localStorage.setItem('collaborativeNote', String(response.id));
        }
      })
    );
  }

  /**
   * Logout method to clear user session and redirect to login page.
   */
  logout(): void {
    this.isAuthenticatedSubject.next(false);
    localStorage.removeItem('collaborativeNote');
    this.router.navigate(['/login']);
  }

  /**
   * Check if the user is authenticated.
   */
  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  /**
   * Get the currently logged-in user's ID.
   */
  getUserId(): number | null {
    return this.userId || Number(localStorage.getItem('collaborativeNote'));
  }

  ifuserIsLogin(): any {
    return localStorage.getItem('collaborativeNote');
  }


  getUserInfo(userId: number){
    return this.http.get<any>(`${this.baseURL}users/${userId}`);
  }

  /**
   * Automatically log in if user credentials exist in localStorage.
   */
  autoLogin(): void {
    const storedUserId = localStorage.getItem('userId');
    if (storedUserId) {
      this.userId = Number(storedUserId);
      this.isAuthenticatedSubject.next(true);
    }
  }

  getNoteList(userId: number){
    return this.http.get<any>(`${this.baseURL}users/${userId}/notes`);
  }
}
