import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { expect } from '@jest/globals';
import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { AppObjectType } from 'app/entities/enumerations/app-object-type/app-object-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';
import { AppObject, IAppObject } from '../app-object.model';

import { AppObjectService } from './app-object.service';

describe('Service Tests', () => {
  describe('AppObject Service', () => {
    let service: AppObjectService;
    let httpMock: HttpTestingController;
    let elemDefault: IAppObject;
    let expectedResult: IAppObject | IAppObject[] | boolean | null;
    let currentDate: dayjs.Dayjs;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [provideHttpClient(), provideHttpClientTesting()],
      });
      expectedResult = null;
      service = TestBed.inject(AppObjectService);
      httpMock = TestBed.inject(HttpTestingController);
      currentDate = dayjs();

      elemDefault = new AppObject(
        0,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        AppObjectType.FUNCTIONAL_MODULE,
        currentDate,
        0,
        StandardRecordStatus.ACTIVE,
        0,
        currentDate,
        currentDate,
      );
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = {
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ ...elemDefault });
      });

      it('should create a AppObject', () => {
        const returnedFromService = {
          id: 0,
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          lastChange: currentDate,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.create(new AppObject()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a AppObject', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          objectType: AppObjectType.FUNCTIONAL_MODULE,
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          quantity: 1,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          lastChange: currentDate,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of AppObject', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          objectType: AppObjectType.FUNCTIONAL_MODULE,
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          quantity: 1,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          lastChange: currentDate,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should return a lov of AppObject', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          objectType: AppObjectType.FUNCTIONAL_MODULE,
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          quantity: 1,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          lastChange: currentDate,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.lov().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should return an export of AppObject', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          objectType: AppObjectType.FUNCTIONAL_MODULE,
          lastChange: currentDate.format(DATE_TIME_FORMAT),
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          quantity: 1,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          lastChange: currentDate,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.exportData().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a AppObject', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addAppObjectToCollectionIfMissing', () => {
        it('should add a AppObject to an empty array', () => {
          const appObject: IAppObject = { id: 26368 };
          expectedResult = service.addAppObjectToCollectionIfMissing([], appObject);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(appObject);
        });

        it('should not add a AppObject to an array that contains it', () => {
          const appObject: IAppObject = { id: 26368 };
          const appObjectCollection: IAppObject[] = [
            {
              ...appObject,
            },
            { id: 9861 },
          ];
          expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, appObject);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a AppObject to an array that doesn't contain it", () => {
          const appObject: IAppObject = { id: 26368 };
          const appObjectCollection: IAppObject[] = [{ id: 9861 }];
          expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, appObject);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(appObject);
        });

        it('should add only unique AppObject to an array', () => {
          const appObjectArray: IAppObject[] = [{ id: 26368 }, { id: 9861 }, { id: 123 }];
          const appObjectCollection: IAppObject[] = [{ id: 9861 }];
          expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, ...appObjectArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const appObject: IAppObject = { id: 26368 };
          const appObject2: IAppObject = { id: 9861 };
          expectedResult = service.addAppObjectToCollectionIfMissing([], appObject, appObject2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(appObject);
          expect(expectedResult).toContain(appObject2);
        });

        it('should accept null and undefined values', () => {
          const appObject: IAppObject = { id: 26368 };
          expectedResult = service.addAppObjectToCollectionIfMissing([], null, appObject, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(appObject);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
