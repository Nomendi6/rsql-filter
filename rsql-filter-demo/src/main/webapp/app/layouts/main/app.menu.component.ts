import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { PrimeNG } from 'primeng/config';
import { Subscription } from 'rxjs';
import { MenubarModule } from 'primeng/menubar';
import { Router } from '@angular/router';
import { THEMES } from 'app/config/theme.constants';
import { ThemeService } from 'app/shared/theme/theme.service';
import { TranslateService } from '@ngx-translate/core';
import { LANGUAGES } from 'app/config/language.constants';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import dayjs from 'dayjs/esm';
import FindLanguageFromKeyPipe from '../../shared/language/find-language-from-key.pipe';
import { AccountService } from '../../core/auth/account.service';
import { LoginService } from '../../login/login.service';
import MainComponent from './main.component';

@Component({
  standalone: true,
  selector: 'app-menu',
  templateUrl: './app.menu.component.html',
  styleUrls: ['./app.menu.component.scss'],
  providers: [FindLanguageFromKeyPipe],
  imports: [MenubarModule],
})
export class AppMenuComponent implements OnInit, OnDestroy {
  public menuItems: MenuItem[] = [];
  private subscriptions: Subscription[] = [];
  private themes = THEMES;
  private languages = LANGUAGES;

  public app = inject(MainComponent);
  public accountService = inject(AccountService);
  public translateService = inject(TranslateService);
  public stateStorageService = inject(StateStorageService);
  private config = inject(PrimeNG);
  private router = inject(Router);
  private loginService = inject(LoginService);
  private themeService = inject(ThemeService);
  private findLanguageFromKeyPipe = inject(FindLanguageFromKeyPipe);

  public ngOnInit(): void {
    this.setMenuItems();

    this.translateService.get('primeng').subscribe(res => this.config.setTranslation(res));

    this.subscriptions.push(
      this.translateService.onLangChange.subscribe((/* params: LangChangeEvent */) => {
        this.setMenuItems();
        this.translateService.get('primeng').subscribe(res => this.config.setTranslation(res));
      }),
    );

    this.subscriptions.push(
      this.accountService.identity().subscribe(() => {
        if (this.accountService.hasAnyAuthority(['ROLE_USER', 'ROLE_ADMIN'])) {
          this.setMenuItems();
        }
      }),
    );
  }

  public onMenuClick(): void {
    this.app.menuClick = true;
  }

  public setMenuItems(): void {
    const isAdmin = this.accountService.hasAnyAuthority(['ROLE_ADMIN']);
    this.menuItems = [
      {
        label: this.translateService.instant('global.menu.home'),
        icon: 'pi pi-fw pi-home',
        routerLink: ['/'],
        automationId: 'home',
      },
      {
        label: '+++',
        icon: 'pi pi-fw pi-briefcase',
        automationId: 'newFormsMenu',
        items: [
          // jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here
        ],
      },
      ...(isAdmin
        ? [
            {
              label: this.translateService.instant('global.menu.admin.main'),
              icon: 'pi pi-fw pi-cog',
              automationId: 'adminMenu',
              items: [
                {
                  label: this.translateService.instant('global.menu.admin.userManagement'),
                  icon: 'pi pi-fw pi-users',
                  routerLink: ['admin/user-management'],
                  automationId: 'admin/userManagement',
                },
                {
                  label: this.translateService.instant('global.menu.admin.metrics'),
                  icon: 'pi pi-fw pi-chart-bar',
                  routerLink: ['admin/metrics'],
                  automationId: 'admin/metrics',
                },
                {
                  label: this.translateService.instant('global.menu.admin.health'),
                  icon: 'pi pi-fw pi-heart',
                  routerLink: ['admin/health'],
                  automationId: 'admin/health',
                },
                {
                  label: this.translateService.instant('global.menu.admin.configuration'),
                  icon: 'pi pi-fw pi-sliders-h',
                  routerLink: ['admin/configuration'],
                  automationId: 'admin/configuration',
                },
                {
                  label: this.translateService.instant('global.menu.admin.logs'),
                  icon: 'pi pi-fw pi-file',
                  routerLink: ['admin/logs'],
                  automationId: 'admin/logs',
                },
                {
                  label: this.translateService.instant('global.menu.admin.apidocs'),
                  icon: 'pi pi-fw pi-share-alt',
                  routerLink: ['admin/docs'],
                  automationId: 'admin/docs',
                },
              ],
            },
          ]
        : []),
      {
        label: this.translateService.instant('global.menu.account.main'),
        icon: 'pi pi-fw pi-user',
        automationId: 'accountMenu',
        items: [
          {
            label: this.translateService.instant('global.menu.account.settings'),
            icon: 'pi pi-fw pi-cog',
            routerLink: ['/account/settings'],
            automationId: 'settings',
          },
          {
            label: this.translateService.instant('global.menu.account.password'),
            icon: 'pi pi-fw pi-lock',
            routerLink: ['/account/password'],
            automationId: 'passwordItem',
          },
          {
            label: this.translateService.instant('global.menu.language'),
            icon: 'pi pi-fw pi-globe',
            automationId: 'languageItem',
            items: this.languages.map(lang => ({
              label: this.findLanguageFromKeyPipe.transform(lang),
              command: () => this.changeLanguage(lang),
              automationId: `${lang}LanguageItem`,
            })),
          },
          {
            label: this.translateService.instant('theme.title'),
            icon: 'pi pi-fw pi-palette',
            automationId: 'themeItem',
            items: this.themes.map(theme => ({
              label: this.translateService.instant(`theme.${theme}`),
              command: () => this.themeService.setTheme(theme),
              automationId: `${theme}ThemeItem`,
            })),
          },
          {
            label: this.translateService.instant('global.menu.account.logout'),
            icon: 'pi pi-fw pi-sign-out',
            command: () => {
              this.loginService.logout();
              this.router.navigate(['']);
            },
            automationId: 'logout',
          },
        ],
      },
    ];
  }

  public changeLanguage(languageKey: string): void {
    this.stateStorageService.storeLocale(languageKey);
    this.translateService.use(languageKey);
    dayjs.locale(languageKey);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}
