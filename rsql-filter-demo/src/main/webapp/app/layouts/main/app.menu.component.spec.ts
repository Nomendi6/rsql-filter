import { expect, jest } from '@jest/globals';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { LocalStorageService } from 'ngx-webstorage';
import { Router, provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { MenuItemCommandEvent } from 'primeng/api';
import { AccountService } from 'app/core/auth/account.service';
import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { LoginService } from '../../login/login.service';
import { AppMenuComponent } from './app.menu.component';
import MainComponent from './main.component';

describe('AppMenuComponent', () => {
  let comp: AppMenuComponent;
  let fixture: ComponentFixture<AppMenuComponent>;
  let accountService: AccountService;
  let loginService: LoginService;
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AppMenuComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        MainComponent,
        AppPageTitleStrategy,
        {
          provide: LocalStorageService,
          useValue: {
            store: jest.fn(),
            retrieve: jest.fn(),
            clear: jest.fn(),
          },
        },
        {
          provide: StateStorageService,
          useValue: {
            storeLocale: jest.fn(),
          },
        },
        {
          provide: LoginService,
          useValue: {
            logout: jest.fn(),
          },
        },
        Router,
      ],
    })
      .overrideTemplate(AppMenuComponent, '')
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppMenuComponent);
    comp = fixture.componentInstance;
    TestBed.inject(MainComponent);
    accountService = TestBed.inject(AccountService);
    loginService = TestBed.inject(LoginService);
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('should set menu items', () => {
    expect(comp.menuItems.length).toBeGreaterThan(0);
  });

  it('should set menuClick to true', () => {
    // GIVEN
    comp.app.menuClick = false;

    // WHEN
    comp.onMenuClick();

    // THEN
    expect(comp.app.menuClick).toBe(true);
  });

  it('should set menu items if user has ROLE_USER or ROLE_ADMIN authority', () => {
    // GIVEN
    jest.spyOn(accountService, 'identity').mockReturnValue(of(null));
    jest.spyOn(accountService, 'hasAnyAuthority').mockReturnValue(true);
    const setMenuItemsSpy = jest.spyOn(comp, 'setMenuItems');

    // WHEN
    comp.ngOnInit();

    // THEN
    expect(setMenuItemsSpy).toHaveBeenCalled();
  });

  it('should change language', () => {
    // GIVEN
    const languageKey = 'en';
    const stateStorageServiceSpy = jest.spyOn(comp.stateStorageService, 'storeLocale');
    const translateServiceSpy = jest.spyOn(comp.translateService, 'use');

    // WHEN
    comp.changeLanguage(languageKey);

    // THEN
    expect(stateStorageServiceSpy).toHaveBeenCalledWith(languageKey);
    expect(translateServiceSpy).toHaveBeenCalledWith(languageKey);
  });

  it('should call the changeLanguage function when a language menu item is clicked', () => {
    // GIVEN
    const changeLanguageSpy = jest.spyOn(comp, 'changeLanguage');

    // Find the English language menu item deeply nested in the menuItems array
    const accountMenuItem = comp.menuItems.find(item => item.automationId === 'accountMenu');
    const languagesMenuItem = accountMenuItem?.items?.find(item => item.automationId === 'languageItem');
    const firstLanguageItem = languagesMenuItem?.items?.[0];
    const menuItemEvent: MenuItemCommandEvent = {
      originalEvent: new Event('click'),
    };

    // WHEN
    if (firstLanguageItem) {
      firstLanguageItem.command?.(menuItemEvent);
    }

    // THEN
    expect(changeLanguageSpy).toHaveBeenCalled();
  });

  it('should call the logout function when the logout menu item is clicked', () => {
    // GIVEN
    const logoutSpy = jest.spyOn(loginService, 'logout');

    // Find the English language menu item deeply nested in the menuItems array
    const accountMenuItem = comp.menuItems.find(item => item.automationId === 'accountMenu');
    const logoutMenuItem = accountMenuItem?.items?.find(item => item.automationId === 'logout');
    const menuItemEvent: MenuItemCommandEvent = {
      originalEvent: new Event('click'),
    };

    // WHEN
    if (logoutMenuItem) {
      logoutMenuItem.command?.(menuItemEvent);
    }

    // THEN
    expect(logoutSpy).toHaveBeenCalled();
  });
});
