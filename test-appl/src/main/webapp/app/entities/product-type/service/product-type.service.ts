import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IProductType, NewProductType } from '../product-type.model';

export type PartialUpdateProductType = Partial<IProductType> & Pick<IProductType, 'id'>;

type RestOf<T extends IProductType | NewProductType> = Omit<T, 'validFrom' | 'validUntil'> & {
  validFrom?: string | null;
  validUntil?: string | null;
};

export type RestProductType = RestOf<IProductType>;

export type NewRestProductType = RestOf<NewProductType>;

export type PartialUpdateRestProductType = RestOf<PartialUpdateProductType>;

export type EntityResponseType = HttpResponse<IProductType>;
export type EntityArrayResponseType = HttpResponse<IProductType[]>;

@Injectable({ providedIn: 'root' })
export class ProductTypeService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/product-types');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(productType: NewProductType): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(productType);
    return this.http
      .post<RestProductType>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(productType: IProductType): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(productType);
    return this.http
      .put<RestProductType>(`${this.resourceUrl}/${this.getProductTypeIdentifier(productType)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(productType: PartialUpdateProductType): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(productType);
    return this.http
      .patch<RestProductType>(`${this.resourceUrl}/${this.getProductTypeIdentifier(productType)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestProductType>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestProductType[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getProductTypeIdentifier(productType: Pick<IProductType, 'id'>): number {
    return productType.id;
  }

  compareProductType(o1: Pick<IProductType, 'id'> | null, o2: Pick<IProductType, 'id'> | null): boolean {
    return o1 && o2 ? this.getProductTypeIdentifier(o1) === this.getProductTypeIdentifier(o2) : o1 === o2;
  }

  addProductTypeToCollectionIfMissing<Type extends Pick<IProductType, 'id'>>(
    productTypeCollection: Type[],
    ...productTypesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const productTypes: Type[] = productTypesToCheck.filter(isPresent);
    if (productTypes.length > 0) {
      const productTypeCollectionIdentifiers = productTypeCollection.map(
        productTypeItem => this.getProductTypeIdentifier(productTypeItem)!
      );
      const productTypesToAdd = productTypes.filter(productTypeItem => {
        const productTypeIdentifier = this.getProductTypeIdentifier(productTypeItem);
        if (productTypeCollectionIdentifiers.includes(productTypeIdentifier)) {
          return false;
        }
        productTypeCollectionIdentifiers.push(productTypeIdentifier);
        return true;
      });
      return [...productTypesToAdd, ...productTypeCollection];
    }
    return productTypeCollection;
  }

  protected convertDateFromClient<T extends IProductType | NewProductType | PartialUpdateProductType>(productType: T): RestOf<T> {
    return {
      ...productType,
      validFrom: productType.validFrom?.toJSON() ?? null,
      validUntil: productType.validUntil?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restProductType: RestProductType): IProductType {
    return {
      ...restProductType,
      validFrom: restProductType.validFrom ? dayjs(restProductType.validFrom) : undefined,
      validUntil: restProductType.validUntil ? dayjs(restProductType.validUntil) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestProductType>): HttpResponse<IProductType> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestProductType[]>): HttpResponse<IProductType[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
