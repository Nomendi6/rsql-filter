import { expect, jest } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { MissingTranslationHandler, TranslateModule, TranslateService } from '@ngx-translate/core';
import { missingTranslationHandler } from 'app/config/translation.config';
import { StandardRecordStatusService } from './standard-record-status.service';

describe.only('StandardRecordStatusService', () => {
  let service: StandardRecordStatusService;

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
      providers: [StandardRecordStatusService, TranslateService],
    });

    service = TestBed.inject(StandardRecordStatusService);
  });

  describe('defineEnums', () => {
    it('should call defineEnums method upon initializing the service', () => {
      // GIVEN
      const defineEnumsSpy = jest.spyOn(StandardRecordStatusService.prototype, 'defineEnums');

      // WHEN
      service = new StandardRecordStatusService(TestBed.inject(TranslateService));

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
      service.standardRecordStatusesSharedCollection = [];

      // WHEN
      service.defineEnums();

      // THEN
      expect(service.standardRecordStatusesSharedCollection).toHaveLength(2);
    });
  });
});
