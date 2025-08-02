import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { expect } from '@jest/globals';
import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';
import { IProduct, Product } from '../product.model';

import { ProductService } from './product.service';

describe('Service Tests', () => {
  describe('Product Service', () => {
    let service: ProductService;
    let httpMock: HttpTestingController;
    let elemDefault: IProduct;
    let expectedResult: IProduct | IProduct[] | boolean | null;
    let currentDate: dayjs.Dayjs;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [provideHttpClient(), provideHttpClientTesting()],
      });
      expectedResult = null;
      service = TestBed.inject(ProductService);
      httpMock = TestBed.inject(HttpTestingController);
      currentDate = dayjs();

      elemDefault = new Product(0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 0, StandardRecordStatus.ACTIVE, currentDate, currentDate);
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

      it('should create a Product', () => {
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

        service.create(new Product()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Product', () => {
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

      it('should return a list of Product', () => {
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

      it('should return a lov of Product', () => {
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

      it('should return an export of Product', () => {
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

      it('should delete a Product', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addProductToCollectionIfMissing', () => {
        it('should add a Product to an empty array', () => {
          const product: IProduct = { id: 21536 };
          expectedResult = service.addProductToCollectionIfMissing([], product);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(product);
        });

        it('should not add a Product to an array that contains it', () => {
          const product: IProduct = { id: 21536 };
          const productCollection: IProduct[] = [
            {
              ...product,
            },
            { id: 11926 },
          ];
          expectedResult = service.addProductToCollectionIfMissing(productCollection, product);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a Product to an array that doesn't contain it", () => {
          const product: IProduct = { id: 21536 };
          const productCollection: IProduct[] = [{ id: 11926 }];
          expectedResult = service.addProductToCollectionIfMissing(productCollection, product);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(product);
        });

        it('should add only unique Product to an array', () => {
          const productArray: IProduct[] = [{ id: 21536 }, { id: 11926 }, { id: 123 }];
          const productCollection: IProduct[] = [{ id: 11926 }];
          expectedResult = service.addProductToCollectionIfMissing(productCollection, ...productArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const product: IProduct = { id: 21536 };
          const product2: IProduct = { id: 11926 };
          expectedResult = service.addProductToCollectionIfMissing([], product, product2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(product);
          expect(expectedResult).toContain(product2);
        });

        it('should accept null and undefined values', () => {
          const product: IProduct = { id: 21536 };
          expectedResult = service.addProductToCollectionIfMissing([], null, product, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(product);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
