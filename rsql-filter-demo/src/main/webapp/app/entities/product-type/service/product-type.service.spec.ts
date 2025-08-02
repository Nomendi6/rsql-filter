import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { expect } from '@jest/globals';
import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';
import { IProductType, ProductType } from '../product-type.model';

import { ProductTypeService } from './product-type.service';

describe('Service Tests', () => {
  describe('ProductType Service', () => {
    let service: ProductTypeService;
    let httpMock: HttpTestingController;
    let elemDefault: IProductType;
    let expectedResult: IProductType | IProductType[] | boolean | null;
    let currentDate: dayjs.Dayjs;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [provideHttpClient(), provideHttpClientTesting()],
      });
      expectedResult = null;
      service = TestBed.inject(ProductTypeService);
      httpMock = TestBed.inject(HttpTestingController);
      currentDate = dayjs();

      elemDefault = new ProductType(0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 0, StandardRecordStatus.ACTIVE, currentDate, currentDate);
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = {
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ ...elemDefault });
      });

      it('should create a ProductType', () => {
        const returnedFromService = {
          id: 0,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.create(new ProductType()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a ProductType', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of ProductType', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should return a lov of ProductType', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.lov().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should return an export of ProductType', () => {
        const returnedFromService = {
          id: 1,
          code: 'BBBBBB',
          name: 'BBBBBB',
          description: 'BBBBBB',
          seq: 1,
          status: StandardRecordStatus.ACTIVE,
          validFrom: currentDate.format(DATE_TIME_FORMAT),
          validUntil: currentDate.format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          ...returnedFromService,
          validFrom: currentDate,
          validUntil: currentDate,
        };

        service.exportData().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a ProductType', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addProductTypeToCollectionIfMissing', () => {
        it('should add a ProductType to an empty array', () => {
          const productType: IProductType = { id: 28647 };
          expectedResult = service.addProductTypeToCollectionIfMissing([], productType);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(productType);
        });

        it('should not add a ProductType to an array that contains it', () => {
          const productType: IProductType = { id: 28647 };
          const productTypeCollection: IProductType[] = [
            {
              ...productType,
            },
            { id: 20361 },
          ];
          expectedResult = service.addProductTypeToCollectionIfMissing(productTypeCollection, productType);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a ProductType to an array that doesn't contain it", () => {
          const productType: IProductType = { id: 28647 };
          const productTypeCollection: IProductType[] = [{ id: 20361 }];
          expectedResult = service.addProductTypeToCollectionIfMissing(productTypeCollection, productType);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(productType);
        });

        it('should add only unique ProductType to an array', () => {
          const productTypeArray: IProductType[] = [{ id: 28647 }, { id: 20361 }, { id: 123 }];
          const productTypeCollection: IProductType[] = [{ id: 20361 }];
          expectedResult = service.addProductTypeToCollectionIfMissing(productTypeCollection, ...productTypeArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const productType: IProductType = { id: 28647 };
          const productType2: IProductType = { id: 20361 };
          expectedResult = service.addProductTypeToCollectionIfMissing([], productType, productType2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(productType);
          expect(expectedResult).toContain(productType2);
        });

        it('should accept null and undefined values', () => {
          const productType: IProductType = { id: 28647 };
          expectedResult = service.addProductTypeToCollectionIfMissing([], null, productType, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(productType);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
