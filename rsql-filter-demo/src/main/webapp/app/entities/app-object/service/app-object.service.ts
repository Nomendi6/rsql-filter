import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import dayjs from 'dayjs/esm';
import { ClassWithId } from 'app/shared/base-entity/class-with-id';
import { BaseEntityWithDateService } from 'app/shared/base-entity/base-entity-with-date.service';

import { isPresent } from 'app/core/util/operators';
import { IAppObject, getAppObjectIdentifier } from '../app-object.model';

export type EntityResponseType = HttpResponse<IAppObject>;
export type EntityArrayResponseType = HttpResponse<IAppObject[]>;

type RestOf<T extends IAppObject> = Omit<T, 'lastChange' | 'validFrom' | 'validUntil'> & {
  lastChange?: string | null;
  validFrom?: string | null;
  validUntil?: string | null;
};

export type RestAppObject = RestOf<IAppObject>;

@Injectable({ providedIn: 'root' })
export class AppObjectService extends BaseEntityWithDateService<IAppObject, RestAppObject> {
  constructor(protected http: HttpClient) {
    super(`${SERVER_API_URL}api/`, 'app-object');
  }

  addAppObjectToCollectionIfMissing(
    appObjectCollection: IAppObject[],
    ...appObjectsToCheck: (IAppObject | null | undefined)[]
  ): IAppObject[] {
    const appObjects: IAppObject[] = appObjectsToCheck.filter(isPresent);
    return this.addArrayToCollectionIfMissing(appObjectCollection, appObjects);
  }

  addArrayToCollectionIfMissing(appObjectCollection: IAppObject[], appObjects: IAppObject[]): IAppObject[] {
    if (appObjects.length > 0) {
      const appObjectCollectionIdentifiers = appObjectCollection.map(appObjectItem => getAppObjectIdentifier(appObjectItem)!);
      const appObjectsToAdd = appObjects.filter(appObjectItem => {
        const appObjectIdentifier = getAppObjectIdentifier(appObjectItem);
        if (appObjectIdentifier == null || appObjectCollectionIdentifiers.includes(appObjectIdentifier)) {
          return false;
        }
        appObjectCollectionIdentifiers.push(appObjectIdentifier);
        return true;
      });
      return [...appObjectsToAdd, ...appObjectCollection];
    }
    return appObjectCollection;
  }

  protected convertDateFromClient(appObject: Partial<IAppObject> & ClassWithId): RestAppObject {
    return {
      ...appObject,
      lastChange: appObject.lastChange?.toJSON() ?? null,
      validFrom: appObject.validFrom?.toJSON() ?? null,
      validUntil: appObject.validUntil?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.lastChange = res.body.lastChange ? dayjs(res.body.lastChange) : undefined;
      res.body.validFrom = res.body.validFrom ? dayjs(res.body.validFrom) : undefined;
      res.body.validUntil = res.body.validUntil ? dayjs(res.body.validUntil) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((appObject: IAppObject) => {
        appObject.lastChange = appObject.lastChange ? dayjs(appObject.lastChange) : undefined;
        appObject.validFrom = appObject.validFrom ? dayjs(appObject.validFrom) : undefined;
        appObject.validUntil = appObject.validUntil ? dayjs(appObject.validUntil) : undefined;
      });
    }
    return res;
  }
}
