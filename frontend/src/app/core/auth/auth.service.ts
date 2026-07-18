import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthenticatedUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;

  private readonly currentUser = signal<AuthenticatedUser | null>(null);
  private sessionRequest$: Observable<boolean> | null = null;

  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  /**
   * Ensures the XSRF-TOKEN cookie exists, then logs in.
   * Session lives in an HttpOnly cookie — never localStorage.
   */
  login(email: string, password: string): Observable<AuthenticatedUser> {
    return this.http.get(`${this.apiBaseUrl}/auth/csrf`).pipe(
      switchMap(() =>
        this.http.post<AuthenticatedUser>(`${this.apiBaseUrl}/auth/login`, { email, password })
      ),
      tap((user) => {
        this.currentUser.set(user);
        this.sessionRequest$ = null;
      })
    );
  }

  ensureSession(): Observable<boolean> {
    if (this.currentUser()) {
      return of(true);
    }

    if (!this.sessionRequest$) {
      this.sessionRequest$ = this.http.get<AuthenticatedUser>(`${this.apiBaseUrl}/auth/me`).pipe(
        tap((user) => this.currentUser.set(user)),
        map(() => true),
        catchError(() => {
          this.currentUser.set(null);
          return of(false);
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
        tap({
          finalize: () => {
            this.sessionRequest$ = null;
          },
        })
      );
    }

    return this.sessionRequest$;
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.get(`${this.apiBaseUrl}/auth/csrf`).pipe(
      switchMap(() =>
        this.http.put<void>(`${this.apiBaseUrl}/auth/password`, { currentPassword, newPassword })
      )
    );
  }

  logout(): Observable<void> {
    return this.http.get(`${this.apiBaseUrl}/auth/csrf`).pipe(
      switchMap(() => this.http.post<void>(`${this.apiBaseUrl}/auth/logout`, {})),
      tap(() => this.clearClientSession()),
      catchError(() => {
        this.clearClientSession();
        return of(undefined);
      }),
      map(() => undefined)
    );
  }

  clearClientSession(): void {
    this.currentUser.set(null);
    this.sessionRequest$ = null;
  }
}
