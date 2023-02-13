import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IAppObject, NewAppObject } from '../app-object.model';

export type PartialUpdateAppObject = Partial<IAppObject> & Pick<IAppObject, 'id'>;

type RestOf<T extends IAppObject | NewAppObject> = Omit<T, 'lastChange' | 'validFrom' | 'validUntil' | 'creationDate'> & {
  lastChange?: string | null;
  validFrom?: string | null;
  validUntil?: string | null;
  creationDate?: string | null;
};

export type RestAppObject = RestOf<IAppObject>;

export type NewRestAppObject = RestOf<NewAppObject>;

export type PartialUpdateRestAppObject = RestOf<PartialUpdateAppObject>;

export type EntityResponseType = HttpResponse<IAppObject>;
export type EntityArrayResponseType = HttpResponse<IAppObject[]>;

@Injectable({ providedIn: 'root' })
export class AppObjectService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/app-objects');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(appObject: NewAppObject): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appObject);
    return this.http
      .post<RestAppObject>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(appObject: IAppObject): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appObject);
    return this.http
      .put<RestAppObject>(`${this.resourceUrl}/${this.getAppObjectIdentifier(appObject)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(appObject: PartialUpdateAppObject): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appObject);
    return this.http
      .patch<RestAppObject>(`${this.resourceUrl}/${this.getAppObjectIdentifier(appObject)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestAppObject>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAppObject[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getAppObjectIdentifier(appObject: Pick<IAppObject, 'id'>): number {
    return appObject.id;
  }

  compareAppObject(o1: Pick<IAppObject, 'id'> | null, o2: Pick<IAppObject, 'id'> | null): boolean {
    return o1 && o2 ? this.getAppObjectIdentifier(o1) === this.getAppObjectIdentifier(o2) : o1 === o2;
  }

  addAppObjectToCollectionIfMissing<Type extends Pick<IAppObject, 'id'>>(
    appObjectCollection: Type[],
    ...appObjectsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const appObjects: Type[] = appObjectsToCheck.filter(isPresent);
    if (appObjects.length > 0) {
      const appObjectCollectionIdentifiers = appObjectCollection.map(appObjectItem => this.getAppObjectIdentifier(appObjectItem)!);
      const appObjectsToAdd = appObjects.filter(appObjectItem => {
        const appObjectIdentifier = this.getAppObjectIdentifier(appObjectItem);
        if (appObjectCollectionIdentifiers.includes(appObjectIdentifier)) {
          return false;
        }
        appObjectCollectionIdentifiers.push(appObjectIdentifier);
        return true;
      });
      return [...appObjectsToAdd, ...appObjectCollection];
    }
    return appObjectCollection;
  }

  protected convertDateFromClient<T extends IAppObject | NewAppObject | PartialUpdateAppObject>(appObject: T): RestOf<T> {
    return {
      ...appObject,
      lastChange: appObject.lastChange?.toJSON() ?? null,
      validFrom: appObject.validFrom?.toJSON() ?? null,
      validUntil: appObject.validUntil?.toJSON() ?? null,
      creationDate: appObject.creationDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restAppObject: RestAppObject): IAppObject {
    return {
      ...restAppObject,
      lastChange: restAppObject.lastChange ? dayjs(restAppObject.lastChange) : undefined,
      validFrom: restAppObject.validFrom ? dayjs(restAppObject.validFrom) : undefined,
      validUntil: restAppObject.validUntil ? dayjs(restAppObject.validUntil) : undefined,
      creationDate: restAppObject.creationDate ? dayjs(restAppObject.creationDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestAppObject>): HttpResponse<IAppObject> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestAppObject[]>): HttpResponse<IAppObject[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
