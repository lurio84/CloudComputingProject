import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
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
   * @param userId - The user ID to login with
   */
  login(form: any): Observable<any> {
    return this.http.post<any>(`${this.baseURL}users`, form).pipe(
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
    return this.userId || Number(localStorage.getItem('userId'));
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
}
