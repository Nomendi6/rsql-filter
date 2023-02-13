import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { DATE_FORMAT } from 'app/config/input.constants';
import { IAppObject } from '../app-object.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../app-object.test-samples';

import { AppObjectService, RestAppObject } from './app-object.service';

const requireRestSample: RestAppObject = {
  ...sampleWithRequiredData,
  lastChange: sampleWithRequiredData.lastChange?.toJSON(),
  validFrom: sampleWithRequiredData.validFrom?.toJSON(),
  validUntil: sampleWithRequiredData.validUntil?.toJSON(),
  creationDate: sampleWithRequiredData.creationDate?.format(DATE_FORMAT),
};

describe('AppObject Service', () => {
  let service: AppObjectService;
  let httpMock: HttpTestingController;
  let expectedResult: IAppObject | IAppObject[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(AppObjectService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a AppObject', () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const appObject = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(appObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AppObject', () => {
      const appObject = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(appObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AppObject', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AppObject', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AppObject', () => {
      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult);
    });

    describe('addAppObjectToCollectionIfMissing', () => {
      it('should add a AppObject to an empty array', () => {
        const appObject: IAppObject = sampleWithRequiredData;
        expectedResult = service.addAppObjectToCollectionIfMissing([], appObject);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(appObject);
      });

      it('should not add a AppObject to an array that contains it', () => {
        const appObject: IAppObject = sampleWithRequiredData;
        const appObjectCollection: IAppObject[] = [
          {
            ...appObject,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, appObject);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AppObject to an array that doesn't contain it", () => {
        const appObject: IAppObject = sampleWithRequiredData;
        const appObjectCollection: IAppObject[] = [sampleWithPartialData];
        expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, appObject);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(appObject);
      });

      it('should add only unique AppObject to an array', () => {
        const appObjectArray: IAppObject[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const appObjectCollection: IAppObject[] = [sampleWithRequiredData];
        expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, ...appObjectArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const appObject: IAppObject = sampleWithRequiredData;
        const appObject2: IAppObject = sampleWithPartialData;
        expectedResult = service.addAppObjectToCollectionIfMissing([], appObject, appObject2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(appObject);
        expect(expectedResult).toContain(appObject2);
      });

      it('should accept null and undefined values', () => {
        const appObject: IAppObject = sampleWithRequiredData;
        expectedResult = service.addAppObjectToCollectionIfMissing([], null, appObject, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(appObject);
      });

      it('should return initial array if no AppObject is added', () => {
        const appObjectCollection: IAppObject[] = [sampleWithRequiredData];
        expectedResult = service.addAppObjectToCollectionIfMissing(appObjectCollection, undefined, null);
        expect(expectedResult).toEqual(appObjectCollection);
      });
    });

    describe('compareAppObject', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAppObject(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareAppObject(entity1, entity2);
        const compareResult2 = service.compareAppObject(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareAppObject(entity1, entity2);
        const compareResult2 = service.compareAppObject(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareAppObject(entity1, entity2);
        const compareResult2 = service.compareAppObject(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
