import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import dayjs from 'dayjs/esm';
import { ClassWithId } from 'app/shared/base-entity/class-with-id';
import { BaseEntityWithDateService } from 'app/shared/base-entity/base-entity-with-date.service';

import { isPresent } from 'app/core/util/operators';
import { IProductType, getProductTypeIdentifier } from '../product-type.model';

export type EntityResponseType = HttpResponse<IProductType>;
export type EntityArrayResponseType = HttpResponse<IProductType[]>;

type RestOf<T extends IProductType> = Omit<T, 'validFrom' | 'validUntil'> & {
  validFrom?: string | null;
  validUntil?: string | null;
};

export type RestProductType = RestOf<IProductType>;

@Injectable({ providedIn: 'root' })
export class ProductTypeService extends BaseEntityWithDateService<IProductType, RestProductType> {
  constructor(protected http: HttpClient) {
    super(`${SERVER_API_URL}api/`, 'product-type');
  }

  addProductTypeToCollectionIfMissing(
    productTypeCollection: IProductType[],
    ...productTypesToCheck: (IProductType | null | undefined)[]
  ): IProductType[] {
    const productTypes: IProductType[] = productTypesToCheck.filter(isPresent);
    return this.addArrayToCollectionIfMissing(productTypeCollection, productTypes);
  }

  addArrayToCollectionIfMissing(productTypeCollection: IProductType[], productTypes: IProductType[]): IProductType[] {
    if (productTypes.length > 0) {
      const productTypeCollectionIdentifiers = productTypeCollection.map(productTypeItem => getProductTypeIdentifier(productTypeItem)!);
      const productTypesToAdd = productTypes.filter(productTypeItem => {
        const productTypeIdentifier = getProductTypeIdentifier(productTypeItem);
        if (productTypeIdentifier == null || productTypeCollectionIdentifiers.includes(productTypeIdentifier)) {
          return false;
        }
        productTypeCollectionIdentifiers.push(productTypeIdentifier);
        return true;
      });
      return [...productTypesToAdd, ...productTypeCollection];
    }
    return productTypeCollection;
  }

  protected convertDateFromClient(productType: Partial<IProductType> & ClassWithId): RestProductType {
    return {
      ...productType,
      validFrom: productType.validFrom?.toJSON() ?? null,
      validUntil: productType.validUntil?.toJSON() ?? null,
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
      res.body.forEach((productType: IProductType) => {
        productType.validFrom = productType.validFrom ? dayjs(productType.validFrom) : undefined;
        productType.validUntil = productType.validUntil ? dayjs(productType.validUntil) : undefined;
      });
    }
    return res;
  }
}
