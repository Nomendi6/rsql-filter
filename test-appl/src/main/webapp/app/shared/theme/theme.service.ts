import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LocalStorageService } from 'ngx-webstorage';
import { ThemeType } from '../../config/theme.constants';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly THEME_KEY = 'theme';
  private currentThemeSubject = new BehaviorSubject<ThemeType>(this.getStoredTheme());
  public currentTheme$: Observable<ThemeType> = this.currentThemeSubject.asObservable();

  constructor(private localStorage: LocalStorageService) {
    this.applyTheme(this.getStoredTheme());

    // Listen for system preference changes
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (this.currentThemeSubject.value === 'system') {
        this.applyTheme('system');
      }
    });
  }

  public setTheme(theme: ThemeType): void {
    this.currentThemeSubject.next(theme);
    this.applyTheme(theme);
  }

  public getStoredTheme(): ThemeType {
    const storedTheme = this.localStorage.retrieve(this.THEME_KEY) as string | null;
    if (storedTheme === 'light' || storedTheme === 'dark') {
      return storedTheme;
    }
    return 'system';
  }

  public applyTheme(theme: ThemeType): void {
    const isDark = theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);

    document.documentElement.classList.toggle('dark', isDark);

    if (theme === 'system') {
      this.localStorage.clear(this.THEME_KEY);
    } else {
      this.localStorage.store(this.THEME_KEY, theme);
    }
  }
}
