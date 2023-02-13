import dayjs from 'dayjs/esm';

import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

import { IProductType, NewProductType } from './product-type.model';

export const sampleWithRequiredData: IProductType = {
  id: 29562,
  code: 'Licensed',
  name: 'Metrics parsing',
};

export const sampleWithPartialData: IProductType = {
  id: 55502,
  code: 'Electronics Kentucky',
  name: 'and Cotton',
  description: '../fake-data/blob/hipster.txt',
  validUntil: dayjs('2023-02-07T01:51'),
};

export const sampleWithFullData: IProductType = {
  id: 68840,
  code: 'hardware Intranet 24/365',
  name: 'Metal',
  description: '../fake-data/blob/hipster.txt',
  seq: 5862,
  status: StandardRecordStatus['ACTIVE'],
  validFrom: dayjs('2023-02-07T08:22'),
  validUntil: dayjs('2023-02-07T04:16'),
};

export const sampleWithNewData: NewProductType = {
  code: 'real-time',
  name: 'unleash Small',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
