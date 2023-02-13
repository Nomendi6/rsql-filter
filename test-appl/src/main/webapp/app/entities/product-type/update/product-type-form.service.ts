import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IProductType, NewProductType } from '../product-type.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IProductType for edit and NewProductTypeFormGroupInput for create.
 */
type ProductTypeFormGroupInput = IProductType | PartialWithRequiredKeyOf<NewProductType>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IProductType | NewProductType> = Omit<T, 'validFrom' | 'validUntil'> & {
  validFrom?: string | null;
  validUntil?: string | null;
};

type ProductTypeFormRawValue = FormValueOf<IProductType>;

type NewProductTypeFormRawValue = FormValueOf<NewProductType>;

type ProductTypeFormDefaults = Pick<NewProductType, 'id' | 'validFrom' | 'validUntil'>;

type ProductTypeFormGroupContent = {
  id: FormControl<ProductTypeFormRawValue['id'] | NewProductType['id']>;
  code: FormControl<ProductTypeFormRawValue['code']>;
  name: FormControl<ProductTypeFormRawValue['name']>;
  description: FormControl<ProductTypeFormRawValue['description']>;
  seq: FormControl<ProductTypeFormRawValue['seq']>;
  status: FormControl<ProductTypeFormRawValue['status']>;
  validFrom: FormControl<ProductTypeFormRawValue['validFrom']>;
  validUntil: FormControl<ProductTypeFormRawValue['validUntil']>;
};

export type ProductTypeFormGroup = FormGroup<ProductTypeFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ProductTypeFormService {
  createProductTypeFormGroup(productType: ProductTypeFormGroupInput = { id: null }): ProductTypeFormGroup {
    const productTypeRawValue = this.convertProductTypeToProductTypeRawValue({
      ...this.getFormDefaults(),
      ...productType,
    });
    return new FormGroup<ProductTypeFormGroupContent>({
      id: new FormControl(
        { value: productTypeRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      code: new FormControl(productTypeRawValue.code, {
        validators: [Validators.required],
      }),
      name: new FormControl(productTypeRawValue.name, {
        validators: [Validators.required],
      }),
      description: new FormControl(productTypeRawValue.description),
      seq: new FormControl(productTypeRawValue.seq),
      status: new FormControl(productTypeRawValue.status),
      validFrom: new FormControl(productTypeRawValue.validFrom),
      validUntil: new FormControl(productTypeRawValue.validUntil),
    });
  }

  getProductType(form: ProductTypeFormGroup): IProductType | NewProductType {
    return this.convertProductTypeRawValueToProductType(form.getRawValue() as ProductTypeFormRawValue | NewProductTypeFormRawValue);
  }

  resetForm(form: ProductTypeFormGroup, productType: ProductTypeFormGroupInput): void {
    const productTypeRawValue = this.convertProductTypeToProductTypeRawValue({ ...this.getFormDefaults(), ...productType });
    form.reset(
      {
        ...productTypeRawValue,
        id: { value: productTypeRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): ProductTypeFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      validFrom: currentTime,
      validUntil: currentTime,
    };
  }

  private convertProductTypeRawValueToProductType(
    rawProductType: ProductTypeFormRawValue | NewProductTypeFormRawValue
  ): IProductType | NewProductType {
    return {
      ...rawProductType,
      validFrom: dayjs(rawProductType.validFrom, DATE_TIME_FORMAT),
      validUntil: dayjs(rawProductType.validUntil, DATE_TIME_FORMAT),
    };
  }

  private convertProductTypeToProductTypeRawValue(
    productType: IProductType | (Partial<NewProductType> & ProductTypeFormDefaults)
  ): ProductTypeFormRawValue | PartialWithRequiredKeyOf<NewProductTypeFormRawValue> {
    return {
      ...productType,
      validFrom: productType.validFrom ? productType.validFrom.format(DATE_TIME_FORMAT) : undefined,
      validUntil: productType.validUntil ? productType.validUntil.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
