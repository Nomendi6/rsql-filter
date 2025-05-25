jest.mock('app/core/auth/account.service');

import { expect, jest } from '@jest/globals';
import { ComponentFixture, TestBed, fakeAsync, inject, tick, waitForAsync } from '@angular/core/testing';
import { HttpHeaders, HttpResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AccountService } from 'app/core/auth/account.service';
import { ConfirmationService } from 'primeng/api';
import { MissingTranslationHandler, TranslateModule, TranslateService } from '@ngx-translate/core';
import { missingTranslationHandler } from 'app/config/translation.config';
import { BreadcrumbService } from 'app/layouts/main/breadcrumb.service';
import { User } from '../user-management.model';
import { UserManagementStoreService } from '../store/user-management-store.service';
import UserManagementComponent from './user-management.component';

describe('User Management Component', () => {
  let comp: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let service: UserManagementStoreService;
  let mockAccountService: AccountService;
  const data = of({
    defaultSort: 'id,asc',
  });
  const queryParamMap = of(
    jest.requireActual<typeof import('@angular/router')>('@angular/router').convertToParamMap({
      page: '1',
      size: '1',
      sort: 'id,desc',
    }),
  );

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        UserManagementComponent,
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
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            data,
            queryParamMap,
            snapshot: {
              queryParamMap: {
                get: (param: string) => '',
              },
            },
          },
        },
        AccountService,
        ConfirmationService,
        TranslateService,
        BreadcrumbService,
      ],
    })
      .overrideTemplate(UserManagementComponent, '')
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserManagementComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(UserManagementStoreService);
    mockAccountService = TestBed.inject(AccountService);
    mockAccountService.identity = jest.fn(() => of(null));
  });

  describe('OnInit', () => {
    it('Should call load all on init', inject(
      [],
      fakeAsync(() => {
        // GIVEN
        const headers = new HttpHeaders().append('link', 'link;link');
        jest.spyOn(service, 'query').mockReturnValue(
          of(
            new HttpResponse({
              body: [new User(123)],
              headers,
            }),
          ),
        );

        // WHEN
        comp.ngOnInit();
        tick(); // simulate async

        // THEN
        expect(service.query).toHaveBeenCalled();
        expect(comp.users[0]).toEqual(expect.objectContaining({ id: 123 }));
      }),
    ));
  });
});
