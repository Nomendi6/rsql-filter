import { expect, jest } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { MissingTranslationHandler, TranslateModule, TranslateService } from '@ngx-translate/core';
import { missingTranslationHandler } from 'app/config/translation.config';
import { AppObjectTypeService } from './app-object-type.service';

describe.only('AppObjectTypeService', () => {
  let service: AppObjectTypeService;

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
      providers: [AppObjectTypeService, TranslateService],
    });

    service = TestBed.inject(AppObjectTypeService);
  });

  describe('defineEnums', () => {
    it('should call defineEnums method upon initializing the service', () => {
      // GIVEN
      const defineEnumsSpy = jest.spyOn(AppObjectTypeService.prototype, 'defineEnums');

      // WHEN
      service = new AppObjectTypeService(TestBed.inject(TranslateService));

      // THEN
      expect(defineEnumsSpy).toHaveBeenCalled();
    });

    it('should call defineEnums method upon language change', () => {
      // GIVEN
      const defineEnumsSpy = jest.spyOn(service, 'defineEnums');

      // WHEN
      TestBed.inject(TranslateService).onLangChange.next({
        lang: 'en',
        translations: {},
      });

      // THEN
      expect(defineEnumsSpy).toHaveBeenCalled();
    });

    it('defineEnums method should add entries in the shared collection array', () => {
      // GIVEN
      service.appObjectTypesSharedCollection = [];

      // WHEN
      service.defineEnums();

      // THEN
      expect(service.appObjectTypesSharedCollection).toHaveLength(4);
    });
  });
});
