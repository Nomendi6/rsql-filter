import dayjs from 'dayjs/esm';

import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

import { IProduct, NewProduct } from './product.model';

export const sampleWithRequiredData: IProduct = {
  id: 77672,
  code: 'Customer-focused',
  name: 'cross-platform Unbranded deposit',
};

export const sampleWithPartialData: IProduct = {
  id: 39641,
  code: 'mindshare Stravenue invoice',
  name: 'Account experiences Garden',
  validFrom: dayjs('2023-02-07T07:59'),
};

export const sampleWithFullData: IProduct = {
  id: 13372,
  code: 'Island Plastic Incredible',
  name: 'Dynamic encompassing vertical',
  description: '../fake-data/blob/hipster.txt',
  seq: 30768,
  status: StandardRecordStatus['ACTIVE'],
  validFrom: dayjs('2023-02-06T15:39'),
  validUntil: dayjs('2023-02-06T18:08'),
};

export const sampleWithNewData: NewProduct = {
  code: 'copying Tuna Marketing',
  name: 'Multi-lateral',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
