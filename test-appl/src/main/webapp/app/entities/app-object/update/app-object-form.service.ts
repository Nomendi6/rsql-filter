import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAppObject, NewAppObject } from '../app-object.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAppObject for edit and NewAppObjectFormGroupInput for create.
 */
type AppObjectFormGroupInput = IAppObject | PartialWithRequiredKeyOf<NewAppObject>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAppObject | NewAppObject> = Omit<T, 'lastChange' | 'validFrom' | 'validUntil'> & {
  lastChange?: string | null;
  validFrom?: string | null;
  validUntil?: string | null;
};

type AppObjectFormRawValue = FormValueOf<IAppObject>;

type NewAppObjectFormRawValue = FormValueOf<NewAppObject>;

type AppObjectFormDefaults = Pick<NewAppObject, 'id' | 'lastChange' | 'validFrom' | 'validUntil' | 'isValid'>;

type AppObjectFormGroupContent = {
  id: FormControl<AppObjectFormRawValue['id'] | NewAppObject['id']>;
  code: FormControl<AppObjectFormRawValue['code']>;
  name: FormControl<AppObjectFormRawValue['name']>;
  description: FormControl<AppObjectFormRawValue['description']>;
  objectType: FormControl<AppObjectFormRawValue['objectType']>;
  lastChange: FormControl<AppObjectFormRawValue['lastChange']>;
  seq: FormControl<AppObjectFormRawValue['seq']>;
  status: FormControl<AppObjectFormRawValue['status']>;
  quantity: FormControl<AppObjectFormRawValue['quantity']>;
  validFrom: FormControl<AppObjectFormRawValue['validFrom']>;
  validUntil: FormControl<AppObjectFormRawValue['validUntil']>;
  isValid: FormControl<AppObjectFormRawValue['isValid']>;
  creationDate: FormControl<AppObjectFormRawValue['creationDate']>;
  parent: FormControl<AppObjectFormRawValue['parent']>;
};

export type AppObjectFormGroup = FormGroup<AppObjectFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AppObjectFormService {
  createAppObjectFormGroup(appObject: AppObjectFormGroupInput = { id: null }): AppObjectFormGroup {
    const appObjectRawValue = this.convertAppObjectToAppObjectRawValue({
      ...this.getFormDefaults(),
      ...appObject,
    });
    return new FormGroup<AppObjectFormGroupContent>({
      id: new FormControl(
        { value: appObjectRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      code: new FormControl(appObjectRawValue.code, {
        validators: [Validators.required],
      }),
      name: new FormControl(appObjectRawValue.name, {
        validators: [Validators.required],
      }),
      description: new FormControl(appObjectRawValue.description),
      objectType: new FormControl(appObjectRawValue.objectType),
      lastChange: new FormControl(appObjectRawValue.lastChange),
      seq: new FormControl(appObjectRawValue.seq),
      status: new FormControl(appObjectRawValue.status),
      quantity: new FormControl(appObjectRawValue.quantity),
      validFrom: new FormControl(appObjectRawValue.validFrom),
      validUntil: new FormControl(appObjectRawValue.validUntil),
      isValid: new FormControl(appObjectRawValue.isValid),
      creationDate: new FormControl(appObjectRawValue.creationDate),
      parent: new FormControl(appObjectRawValue.parent),
    });
  }

  getAppObject(form: AppObjectFormGroup): IAppObject | NewAppObject {
    return this.convertAppObjectRawValueToAppObject(form.getRawValue() as AppObjectFormRawValue | NewAppObjectFormRawValue);
  }

  resetForm(form: AppObjectFormGroup, appObject: AppObjectFormGroupInput): void {
    const appObjectRawValue = this.convertAppObjectToAppObjectRawValue({ ...this.getFormDefaults(), ...appObject });
    form.reset(
      {
        ...appObjectRawValue,
        id: { value: appObjectRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): AppObjectFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      lastChange: currentTime,
      validFrom: currentTime,
      validUntil: currentTime,
      isValid: false,
    };
  }

  private convertAppObjectRawValueToAppObject(rawAppObject: AppObjectFormRawValue | NewAppObjectFormRawValue): IAppObject | NewAppObject {
    return {
      ...rawAppObject,
      lastChange: dayjs(rawAppObject.lastChange, DATE_TIME_FORMAT),
      validFrom: dayjs(rawAppObject.validFrom, DATE_TIME_FORMAT),
      validUntil: dayjs(rawAppObject.validUntil, DATE_TIME_FORMAT),
    };
  }

  private convertAppObjectToAppObjectRawValue(
    appObject: IAppObject | (Partial<NewAppObject> & AppObjectFormDefaults)
  ): AppObjectFormRawValue | PartialWithRequiredKeyOf<NewAppObjectFormRawValue> {
    return {
      ...appObject,
      lastChange: appObject.lastChange ? appObject.lastChange.format(DATE_TIME_FORMAT) : undefined,
      validFrom: appObject.validFrom ? appObject.validFrom.format(DATE_TIME_FORMAT) : undefined,
      validUntil: appObject.validUntil ? appObject.validUntil.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
