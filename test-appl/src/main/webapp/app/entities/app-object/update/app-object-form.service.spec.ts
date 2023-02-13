import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../app-object.test-samples';

import { AppObjectFormService } from './app-object-form.service';

describe('AppObject Form Service', () => {
  let service: AppObjectFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AppObjectFormService);
  });

  describe('Service methods', () => {
    describe('createAppObjectFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAppObjectFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
            objectType: expect.any(Object),
            lastChange: expect.any(Object),
            seq: expect.any(Object),
            status: expect.any(Object),
            quantity: expect.any(Object),
            validFrom: expect.any(Object),
            validUntil: expect.any(Object),
            isValid: expect.any(Object),
            creationDate: expect.any(Object),
            parent: expect.any(Object),
          })
        );
      });

      it('passing IAppObject should create a new form with FormGroup', () => {
        const formGroup = service.createAppObjectFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
            objectType: expect.any(Object),
            lastChange: expect.any(Object),
            seq: expect.any(Object),
            status: expect.any(Object),
            quantity: expect.any(Object),
            validFrom: expect.any(Object),
            validUntil: expect.any(Object),
            isValid: expect.any(Object),
            creationDate: expect.any(Object),
            parent: expect.any(Object),
          })
        );
      });
    });

    describe('getAppObject', () => {
      it('should return NewAppObject for default AppObject initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createAppObjectFormGroup(sampleWithNewData);

        const appObject = service.getAppObject(formGroup) as any;

        expect(appObject).toMatchObject(sampleWithNewData);
      });

      it('should return NewAppObject for empty AppObject initial value', () => {
        const formGroup = service.createAppObjectFormGroup();

        const appObject = service.getAppObject(formGroup) as any;

        expect(appObject).toMatchObject({});
      });

      it('should return IAppObject', () => {
        const formGroup = service.createAppObjectFormGroup(sampleWithRequiredData);

        const appObject = service.getAppObject(formGroup) as any;

        expect(appObject).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAppObject should not enable id FormControl', () => {
        const formGroup = service.createAppObjectFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAppObject should disable id FormControl', () => {
        const formGroup = service.createAppObjectFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
