import { Component, OnDestroy, OnInit } from '@angular/core';
import { LoginService } from 'app/login/login.service';
import { Router } from '@angular/router';
import { AccountService } from 'app/core/auth/account.service';
import { LANGUAGES } from 'app/config/language.constants';
import { TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { SessionStorageService } from 'ngx-webstorage';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { DialogService } from 'primeng/dynamicdialog';
import { EventManager } from 'app/core/util/event-manager.service';
import { Subscription } from 'rxjs';
import { MainComponent } from './main.component';

@Component({
  providers: [DialogService],
  selector: 'app-topbar',
  template: `
    <div class="layout-topbar" data-cy="topbar">
      <div class="layout-topbar-wrapper">
        <div class="layout-topbar-left">
          <div class="layout-topbar-logo-wrapper">
            <a href="#" class="layout-topbar-logo">
              <img src="content/images/circleW_80_80_admin.png" alt="mirage-layout" />
              <span class="app-name" appTranslate="global.title">Testappl</span>
            </a>
          </div>

          <a href="#" class="sidebar-menu-button" (click)="app.onMenuButtonClick($event)">
            <em class="pi pi-bars"></em>
          </a>

          <!--
                    <a href="#" class="megamenu-mobile-button" (click)="app.onMegaMenuMobileButtonClick($event)">
                      <em class="pi pi-align-right megamenu-icon"></em>
                    </a>
          -->

          <a href="#" class="topbar-menu-mobile-button" (click)="app.onTopbarMobileMenuButtonClick($event)">
            <em class="pi pi-ellipsis-v"></em>
          </a>

          <!--
          <div class="layout-megamenu-wrapper">
            <a class="layout-megamenu-button" href="#" (click)="app.onMegaMenuButtonClick($event)">
              <em class="pi pi-comment"></em>
              Mega Menu
            </a>
            <ul class="layout-megamenu" [ngClass]="{'layout-megamenu-active fadeInDown': app.megaMenuActive}"
                (click)="app.onMegaMenuClick($event)">
              <li [ngClass]="{'active-topmenuitem': activeItem === 1}" (click)="mobileMegaMenuItemClick(1)">
                <a href="#">JavaServer Faces <em class="pi pi-angle-down"></em></a>
                <ul>
                  <li class="active-row ">
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>PrimeFaces</h5>
                                        <span>UI Components for JSF</span>
                                    </span>
                  </li>
                  <li>
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>Premium Templates</h5>
                                        <span>UI Components for JSF</span>
                                    </span>
                  </li>
                  <li>
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>Extensions</h5>
                                        <span>UI Components for JSF</span>
                                    </span>
                  </li>
                </ul>
              </li>
              <li [ngClass]="{'active-topmenuitem': activeItem === 2}" (click)="mobileMegaMenuItemClick(2)">
                <a href="#">Angular <em class="pi pi-angle-down"></em></a>
                <ul>
                  <li>
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>PrimeNG</h5>
                                        <span>UI Components for Angular</span>
                                    </span>

                  </li>
                  <li>
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>Premium Templates</h5>
                                        <span>UI Components for Angular</span>
                                    </span>
                  </li>
                </ul>
              </li>
              <li [ngClass]="{'active-topmenuitem': activeItem === 3}" (click)="mobileMegaMenuItemClick(3)">
                <a href="#">React <em class="pi pi-angle-down"></em></a>
                <ul>
                  <li>
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>PrimeReact</h5>
                                        <span>UI Components for React</span>
                                    </span>
                  </li>
                  <li class="active-row">
                    <em class="pi pi-circle-on"></em>
                    <span>
                                        <h5>Premium Templates</h5>
                                        <span>UI Components for React</span>
                                    </span>
                  </li>
                </ul>
              </li>
            </ul>
          </div>
-->
        </div>

        <div class="layout-topbar-right fadeInDown" [ngSwitch]="isAuthenticated()">
          <ul class="layout-topbar-actions">
            <!--search-->
            <!--
            <li #search class="search-item topbar-item" [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === search }">
              <a href="#" class="topbar-search-mobile-button" (click)="app.onTopbarItemClick($event, search)">
                <em class="topbar-icon pi pi-search"></em>
              </a>
              <ul class="search-item-submenu fadeInDown" (click)="app.topbarItemClick = true">
                <li>
                  <span class="md-inputfield search-input-wrapper">
                    <input pInputText class="w-full" placeholder="Search..." />
                    <em class="pi pi-search"></em>
                  </span>
                </li>
              </ul>
            </li>
-->
            <!--calendar-->
            <!--
            <li #calendar class="topbar-item" [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === calendar }">
              <a href="#" (click)="app.onTopbarItemClick($event, calendar)">
                <em class="topbar-icon pi pi-calendar"></em>
              </a>
              <ul class="fadeInDown" (click)="app.topbarItemClick = true">
                <li class="layout-submenu-header">
                  <h1>Calendar</h1>
                </li>
                <li class="calendar">
                  <p-datepicker [inline]="true"></p-datepicker>
                </li>
              </ul>
            </li>
-->

            <!--message-->
            <!--            <li #message class="topbar-item" [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === message }">
              <a href="#" (click)="app.onTopbarItemClick($event, message)">
                <em class="topbar-icon pi pi-inbox"></em>
              </a>
              <ul class="fadeInDown">
                <li class="layout-submenu-header">
                  <h1>Messages</h1>
                  <span>Today, you have new 4 messages</span>
                </li>
                <li class="layout-submenu-item">
                  <img src="content/layout/images/topbar/avatar-cayla.png" alt="mirage-layout" width="35" />
                  <div class="menu-text">
                    <p>Override the digital divide</p>
                    <span>Cayla Brister</span>
                  </div>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-item">
                  <img src="content/layout/images/topbar/avatar-gabie.png" alt="mirage-layout" width="35" />
                  <div class="menu-text">
                    <p>Nanotechnology immersion</p>
                    <span>Gabie Sheber</span>
                  </div>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-item">
                  <img src="content/layout/images/topbar/avatar-gaspar.png" alt="mirage-layout" width="35" />
                  <div class="menu-text">
                    <p>User generated content</p>
                    <span>Gaspar Antunes</span>
                  </div>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-item">
                  <img src="content/layout/images/topbar/avatar-tatiana.png" alt="mirage-layout" width="35" />
                  <div class="menu-text">
                    <p>The holistic world view</p>
                    <span>Tatiana Gagelman</span>
                  </div>
                  <em class="pi pi-angle-right"></em>
                </li>
              </ul>
            </li>-->

            <!--gift-->
            <!--<li #gift class="topbar-item" [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === gift }">
              <a href="#" (click)="app.onTopbarItemClick($event, gift)">
                <em class="topbar-icon pi pi-envelope"></em>
              </a>
              <ul class="fadeInDown">
                <li class="layout-submenu-header">
                  <h1>Deals</h1>
                </li>

                <li class="deals">
                  <ul>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-sapphire.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Sapphire</p>
                        <span>Angular</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-roma.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Roma</p>
                        <span>Minimalism</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-babylon.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Babylon</p>
                        <span>Powerful</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                  </ul>
                  <ul>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-harmony.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Harmony</p>
                        <span>USWDS</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-prestige.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Prestige</p>
                        <span>Elegancy</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                    <li>
                      <img src="content/layout/images/topbar/deal-icon-ultima.png" alt="mirage-layout" width="35" />
                      <div class="menu-text">
                        <p>Ultima</p>
                        <span>Material</span>
                      </div>
                      <em class="pi pi-angle-right"></em>
                    </li>
                  </ul>
                </li>
              </ul>
            </li>-->

            <!--language-->
            <li
              #languageMenu
              data-cy="tbi-languageMenu"
              class="topbar-item"
              [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === languageMenu }"
            >
              <a href="#" (click)="app.onTopbarItemClick($event, languageMenu)">
                <em class="topbar-icon pi pi-globe"></em>
              </a>
              <ul class="fadeInDown">
                <li class="layout-submenu-header">
                  <h1 appTranslate="global.menu.language">Language</h1>
                </li>

                <li *ngFor="let language of definedLanguages" class="layout-submenu-item" data-cy="tbi-language-{{ language }}">
                  <!--<a href="javascript:void(0);" data-cy="language" (click)="changeLanguage(language)">-->
                  <div (click)="changeLanguage(language)">
                    <div class="menu-text" [ngClass]="{ 'active-language': app.activeLanguage === language }">
                      <!--<div class="menu-text" [ngClass]="{'active-menuitem': app.activeLanguage === language }">-->
                      <p>{{ language | findLanguageFromKey }}</p>
                      <span></span>
                    </div>
                  </div>
                  <!--</a>-->
                </li>
              </ul>
            </li>

            <li #profile class="topbar-item profile-item" [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === profile }">
              <a href="#" (click)="app.onTopbarItemClick($event, profile)" data-cy="tbi-profileMenu">
                <span class="profile-image-wrapper">
                  <!--<img src="content/layout/images/topbar/avatar-eklund.png" alt="mirage-layout" />-->
                  <p-avatar
                    [label]="userInitials"
                    styleClass="mr-2"
                    size="large"
                    [style]="{ 'background-color': '#AC5700', color: '#ffffff' }"
                    shape="circle"
                  ></p-avatar>
                </span>
                <span class="profile-info-wrapper">
                  <h3>{{ userName }}</h3>
                </span>
              </a>
              <ul class="profile-item-submenu fadeInDown">
                <li class="layout-submenu-header">
                  <h1 appTranslate="global.menu.account.main">Account</h1>
                </li>

                <!--
                <li class="profile-submenu-header">
                  <div class="avatar">
                    <div *ngIf="!!imageUrl">
                      <img [src]="imageUrl" alt="profile-image" width="40" />
                    </div>
                    <div *ngIf="!imageUrl">
                      <p-avatar
                        [label]="userInitials"
                        size="xlarge"
                        [style]="{ 'background-color': '#2196F3', color: '#ffffff' }"
                        shape="circle"
                      ></p-avatar>
                    </div>
                  </div>
                  <div class="profile">
                    &lt;!&ndash;                    <img src="content/layout/images/topbar/avatar-eklund.png" alt="mirage-layout" width="40" />&ndash;&gt;
                    <h3 [style]="{ color: '#ffffff' }">{{userName}}</h3>
                    &lt;!&ndash;<span>Design</span>&ndash;&gt;
                  </div>
                </li>
-->
                <li class="layout-submenu-item">
                  <em class="pi pi-user-edit icon icon-1"></em>
                  <a routerLink="account/settings" routerLinkActive="active" data-cy="settings">
                    <div class="menu-text">
                      <p appTranslate="global.menu.account.settings">Settings</p>
                      <span appTranslate="global.menu.account.settings.description">Translation missing for global.menu.account.settings.description</span>
                    </div>
                  </a>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-item">
                  <em class="pi pi-lock icon icon-2"></em>
                  <a routerLink="account/password" routerLinkActive="active" data-cy="passwordItem">
                    <div class="menu-text">
                      <p appTranslate="global.menu.account.password">Password</p>
                      <span appTranslate="global.menu.account.password.description">Translation missing for global.menu.account.password.description</span>
                    </div>
                  </a>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-footer">
                  <p-button type="button" data-cy="logout" (onClick)="logout()" label="{{ 'global.menu.account.logout' | translate }}"></p-button>
                </li>
              </ul>
            </li>
            <li class="layout-submenu-footer" *ngSwitchCase="false">
              <p-button type="button" data-cy="login" (onClick)="login()" label="{{ 'global.menu.account.login' | translate }}"></p-button>
            </li>
            <li>
              <a href="#" class="layout-rightpanel-button" (click)="app.onRightPanelButtonClick($event)">
                <em class="pi pi-arrow-left"></em>
              </a>
            </li>
          </ul>

          <ul class="profile-mobile-wrapper">
            <li
              #mobileProfile
              data-cy="tbi-mobile-profileMenu"
              class="topbar-item profile-item"
              [ngClass]="{ 'active-topmenuitem': app.activeTopbarItem === mobileProfile }"
            >
              <a href="#" (click)="app.onTopbarItemClick($event, mobileProfile)">
                <span class="profile-image-wrapper">
                  <!--<img src="content/layout/images/topbar/avatar-eklund.png" alt="mirage-layout" />-->
                  <p-avatar
                    [label]="userInitials"
                    styleClass="mr-2"
                    size="xlarge"
                    [style]="{ 'background-color': '#2196F3', color: '#ffffff' }"
                    shape="circle"
                  ></p-avatar>
                </span>
                <span class="profile-info-wrapper">
                  <h3>{{ userName }}</h3>
                </span>
              </a>
              <ul class="fadeInDown">
                <li class="profile-submenu-header">
                  <h3 [style]="{ color: '#ffffff' }" appTranslate="global.menu.account.main">Account</h3>
                </li>

                <!--
                <li class="profile-submenu-header">
                  <div class="avatar">
                    <div *ngIf="!!imageUrl">
                      <img [src]="imageUrl" alt="profile-image" width="40" />
                    </div>
                    <div *ngIf="!imageUrl">
                      <p-avatar
                        [label]="userInitials"
                        size="xlarge"
                        [style]="{ 'background-color': '#2196F3', color: '#ffffff' }"
                        shape="circle"
                      ></p-avatar>
                    </div>
                  </div>
                  <div class="profile">
                    &lt;!&ndash;                    <img src="content/layout/images/topbar/avatar-eklund.png" alt="mirage-layout" width="40" />&ndash;&gt;
                    <h3 [style]="{ color: '#ffffff' }">{{userName}}</h3>
                    &lt;!&ndash;<span>Design</span>&ndash;&gt;
                  </div>
                </li>
-->
                <li class="layout-submenu-item">
                  <em class="pi pi-user-edit icon icon-1"></em>
                  <a routerLink="account/settings" routerLinkActive="active" data-cy="mobile-settings">
                    <div class="menu-text">
                      <p appTranslate=""global.menu.account.settings">Translation missing for &quot;global.menu.account.settings</p>
                      <span appTranslate=""global.menu.account.settings.description">Translation missing for &quot;global.menu.account.settings.description</span>
                    </div>
                  </a>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-item">
                  <em class="pi pi-lock icon icon-2"></em>
                  <a routerLink="account/password" routerLinkActive="active" data-cy="passwordItem">
                    <div class="menu-text">
                      <p appTranslate="global.menu.account.password">Password</p>
                      <span appTranslate="global.menu.account.password.description">Translation missing for global.menu.account.password.description</span>
                    </div>
                  </a>
                  <em class="pi pi-angle-right"></em>
                </li>
                <li class="layout-submenu-footer">
                  <button type="button" class="signout-button" data-cy="logout" (click)="logout()" appTranslate="global.menu.account.logout">
                    Sign out
                  </button>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .active-language {
        font-weight: bold;
      }
    `,
  ],
})
export class AppTopBarComponent implements OnInit, OnDestroy {
  displaySetOrgContext = false;
  activeItem = 0;
  definedLanguages = LANGUAGES;
  inProduction? = false;
  openAPIEnabled? = false;
  userName = '----- --------';
  userInitials = '##';
  imageUrl = '';

  logger = console;
  eventSubscriptions: Subscription[] = [];

  constructor(
    public app: MainComponent,
    private loginService: LoginService,
    private router: Router,
    private accountService: AccountService,
    private translateService: TranslateService,
    private sessionStorage: SessionStorageService,
    private profileService: ProfileService,
    protected eventManager: EventManager,
    public dialogService: DialogService,
  ) {}

  ngOnInit(): void {
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.openAPIEnabled = profileInfo.openAPIEnabled;
    });

    this.accountService.identity().subscribe(idInfo => {
      if (idInfo) {
        const firstName: string = idInfo.firstName ?? '';
        const lastName: string = idInfo.lastName ?? '';

        this.userName = `${firstName} ${lastName}`;
        this.imageUrl = idInfo.imageUrl ?? '';

        let initials = '';
        if (firstName.length > 0) {
          initials = firstName.slice(0, 1);
        }
        if (lastName.length > 0) {
          initials = initials + lastName.slice(0, 1);
        }
        this.userInitials = initials;
      }
    });
  }

  ngOnDestroy(): void {
    this.eventSubscriptions.forEach(sub => {
      this.eventManager.destroy(sub);
    });
  }

  mobileMegaMenuItemClick(index: any): void {
    this.app.megaMenuMobileClick = true;
    this.activeItem = this.activeItem === index ? null : index;
  }

  logout(): void {
    this.loginService.logout();
    // this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  changeLanguage(languageKey: string): void {
    this.sessionStorage.store('locale', languageKey);
    this.translateService.use(languageKey);
    dayjs.locale(languageKey);
  }
}
