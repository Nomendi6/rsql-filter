import { Component, OnInit, Renderer2, RendererFactory2, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { AccountService } from 'app/core/auth/account.service';
import { MenuItem } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { FormHelpDialogComponent } from 'app/shared/form-help-dialog/form-help-dialog.component';
import { AppMenuComponent } from './app.menu.component';

@Component({
  standalone: true,
  selector: 'app-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, ToastModule, ConfirmDialogModule, AppMenuComponent, FormHelpDialogComponent],
})
export default class MainComponent implements OnInit {
  appName = '';
  menuItems: MenuItem[] = [];
  horizontalMenu = false;
  darkMode = false;
  menuColorMode = 'dark';
  menuColor = 'layout-menu-dark';
  themeColor = 'blue';
  layoutColor = 'blue';
  rightPanelClick = false;
  rightPanelActive = false;
  menuClick = false;
  staticMenuActive = false;
  menuMobileActive = false;
  megaMenuClick = false;
  megaMenuActive = false;
  megaMenuMobileClick = false;
  megaMenuMobileActive = false;
  topbarItemClick = false;
  topbarMobileMenuClick = false;
  topbarMobileMenuActive = false;
  sidebarActive = false;
  activeTopbarItem: any;
  topbarMenuActive = false;
  menuHoverActive = false;
  configActive = false;
  configClick = false;
  ripple = true;
  inputStyle = 'outlined';
  activeLanguage = 'en';
  public isLoadingUser = false;
  private renderer: Renderer2;

  private router = inject(Router);
  private appPageTitleStrategy = inject(AppPageTitleStrategy);
  private accountService = inject(AccountService);
  private translateService = inject(TranslateService);
  private rootRenderer = inject(RendererFactory2);

  constructor() {
    this.renderer = this.rootRenderer.createRenderer(document.querySelector('html'), null);
  }

  ngOnInit(): void {
    this.setMenuItems();

    // try to log in automatically
    this.isLoadingUser = true;
    this.accountService.identity().subscribe({
      complete: () => {
        this.isLoadingUser = false;
      },
    });

    this.translateService.onLangChange.subscribe((langChangeEvent: LangChangeEvent) => {
      this.appPageTitleStrategy.updateTitle(this.router.routerState.snapshot);
      dayjs.locale(langChangeEvent.lang);
      this.renderer.setAttribute(document.querySelector('html'), 'lang', langChangeEvent.lang);
    });
  }

  public isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  private setMenuItems(): void {
    this.menuItems = [
      {
        label: 'Home',
        icon: 'pi pi-fw pi-home',
        routerLink: ['/'],
      },
      {
        label: 'Health',
        icon: 'pi pi-fw pi-heart',
        routerLink: ['admin/health'],
      },
    ];
  }
}
