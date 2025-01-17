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

  constructor(private http: HttpClient, private router: Router) {}

  /**
   * Login method to authenticate the user.
   * @param userId - The user ID to login with
   */
  login(userId: number): Observable<any> {
    return this.http.post<any>('http://localhost:8080/login', { userId }).pipe(
      tap((response) => {
        if (response.success) {
          this.isAuthenticatedSubject.next(true);
          this.userId = userId;
          localStorage.setItem('userId', String(userId));
        }
      })
    );
  }

  /**
   * Logout method to clear user session and redirect to login page.
   */
  logout(): void {
    this.isAuthenticatedSubject.next(false);
    this.userId = null;
    localStorage.removeItem('userId');
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
