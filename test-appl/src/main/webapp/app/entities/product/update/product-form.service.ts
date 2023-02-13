import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IProduct, NewProduct } from '../product.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IProduct for edit and NewProductFormGroupInput for create.
 */
type ProductFormGroupInput = IProduct | PartialWithRequiredKeyOf<NewProduct>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IProduct | NewProduct> = Omit<T, 'validFrom' | 'validUntil'> & {
  validFrom?: string | null;
  validUntil?: string | null;
};

type ProductFormRawValue = FormValueOf<IProduct>;

type NewProductFormRawValue = FormValueOf<NewProduct>;

type ProductFormDefaults = Pick<NewProduct, 'id' | 'validFrom' | 'validUntil'>;

type ProductFormGroupContent = {
  id: FormControl<ProductFormRawValue['id'] | NewProduct['id']>;
  code: FormControl<ProductFormRawValue['code']>;
  name: FormControl<ProductFormRawValue['name']>;
  description: FormControl<ProductFormRawValue['description']>;
  seq: FormControl<ProductFormRawValue['seq']>;
  status: FormControl<ProductFormRawValue['status']>;
  validFrom: FormControl<ProductFormRawValue['validFrom']>;
  validUntil: FormControl<ProductFormRawValue['validUntil']>;
  tproduct: FormControl<ProductFormRawValue['tproduct']>;
  parent: FormControl<ProductFormRawValue['parent']>;
};

export type ProductFormGroup = FormGroup<ProductFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ProductFormService {
  createProductFormGroup(product: ProductFormGroupInput = { id: null }): ProductFormGroup {
    const productRawValue = this.convertProductToProductRawValue({
      ...this.getFormDefaults(),
      ...product,
    });
    return new FormGroup<ProductFormGroupContent>({
      id: new FormControl(
        { value: productRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      code: new FormControl(productRawValue.code, {
        validators: [Validators.required],
      }),
      name: new FormControl(productRawValue.name, {
        validators: [Validators.required],
      }),
      description: new FormControl(productRawValue.description),
      seq: new FormControl(productRawValue.seq),
      status: new FormControl(productRawValue.status),
      validFrom: new FormControl(productRawValue.validFrom),
      validUntil: new FormControl(productRawValue.validUntil),
      tproduct: new FormControl(productRawValue.tproduct),
      parent: new FormControl(productRawValue.parent),
    });
  }

  getProduct(form: ProductFormGroup): IProduct | NewProduct {
    return this.convertProductRawValueToProduct(form.getRawValue() as ProductFormRawValue | NewProductFormRawValue);
  }

  resetForm(form: ProductFormGroup, product: ProductFormGroupInput): void {
    const productRawValue = this.convertProductToProductRawValue({ ...this.getFormDefaults(), ...product });
    form.reset(
      {
        ...productRawValue,
        id: { value: productRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): ProductFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      validFrom: currentTime,
      validUntil: currentTime,
    };
  }

  private convertProductRawValueToProduct(rawProduct: ProductFormRawValue | NewProductFormRawValue): IProduct | NewProduct {
    return {
      ...rawProduct,
      validFrom: dayjs(rawProduct.validFrom, DATE_TIME_FORMAT),
      validUntil: dayjs(rawProduct.validUntil, DATE_TIME_FORMAT),
    };
  }

  private convertProductToProductRawValue(
    product: IProduct | (Partial<NewProduct> & ProductFormDefaults)
  ): ProductFormRawValue | PartialWithRequiredKeyOf<NewProductFormRawValue> {
    return {
      ...product,
      validFrom: product.validFrom ? product.validFrom.format(DATE_TIME_FORMAT) : undefined,
      validUntil: product.validUntil ? product.validUntil.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
