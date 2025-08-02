import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import dayjs from 'dayjs/esm';
import { ClassWithId } from 'app/shared/base-entity/class-with-id';
import { BaseEntityWithDateService } from 'app/shared/base-entity/base-entity-with-date.service';

import { isPresent } from 'app/core/util/operators';
import { IProduct, getProductIdentifier } from '../product.model';

export type EntityResponseType = HttpResponse<IProduct>;
export type EntityArrayResponseType = HttpResponse<IProduct[]>;

type RestOf<T extends IProduct> = Omit<T, 'validFrom' | 'validUntil'> & {
  validFrom?: string | null;
  validUntil?: string | null;
};

export type RestProduct = RestOf<IProduct>;

@Injectable({ providedIn: 'root' })
export class ProductService extends BaseEntityWithDateService<IProduct, RestProduct> {
  constructor(protected http: HttpClient) {
    super(`${SERVER_API_URL}api/`, 'product');
  }

  addProductToCollectionIfMissing(productCollection: IProduct[], ...productsToCheck: (IProduct | null | undefined)[]): IProduct[] {
    const products: IProduct[] = productsToCheck.filter(isPresent);
    return this.addArrayToCollectionIfMissing(productCollection, products);
  }

  addArrayToCollectionIfMissing(productCollection: IProduct[], products: IProduct[]): IProduct[] {
    if (products.length > 0) {
      const productCollectionIdentifiers = productCollection.map(productItem => getProductIdentifier(productItem)!);
      const productsToAdd = products.filter(productItem => {
        const productIdentifier = getProductIdentifier(productItem);
        if (productIdentifier == null || productCollectionIdentifiers.includes(productIdentifier)) {
          return false;
        }
        productCollectionIdentifiers.push(productIdentifier);
        return true;
      });
      return [...productsToAdd, ...productCollection];
    }
    return productCollection;
  }

  protected convertDateFromClient(product: Partial<IProduct> & ClassWithId): RestProduct {
    return {
      ...product,
      validFrom: product.validFrom?.toJSON() ?? null,
      validUntil: product.validUntil?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.validFrom = res.body.validFrom ? dayjs(res.body.validFrom) : undefined;
      res.body.validUntil = res.body.validUntil ? dayjs(res.body.validUntil) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((product: IProduct) => {
        product.validFrom = product.validFrom ? dayjs(product.validFrom) : undefined;
        product.validUntil = product.validUntil ? dayjs(product.validUntil) : undefined;
      });
    }
    return res;
  }
}
