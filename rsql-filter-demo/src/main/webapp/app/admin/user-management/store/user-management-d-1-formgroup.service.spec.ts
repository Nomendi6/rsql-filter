import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { MissingTranslationHandler, TranslateModule, TranslateService } from '@ngx-translate/core';
import { missingTranslationHandler } from 'app/config/translation.config';
import { expect } from '@jest/globals';
import { SessionStorageService } from 'ngx-webstorage';
import { UserManagement$D$1FormGroupService } from './user-management-d-1-formgroup.service';

describe('UserManagement$D$1FormGroupService', () => {
  let service: UserManagement$D$1FormGroupService;
  let translateService: TranslateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot({
          missingTranslationHandler: {
            provide: MissingTranslationHandler,
            useFactory: missingTranslationHandler,
          },
        }),
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        UserManagement$D$1FormGroupService,
        SessionStorageService,
        TranslateService,
      ],
    });
    service = TestBed.inject(UserManagement$D$1FormGroupService);
    translateService = TestBed.inject(TranslateService);
    translateService.setDefaultLang('en');
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
