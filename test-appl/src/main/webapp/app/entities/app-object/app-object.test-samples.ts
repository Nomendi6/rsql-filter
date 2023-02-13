import dayjs from 'dayjs/esm';

import { AppObjectType } from 'app/entities/enumerations/app-object-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

import { IAppObject, NewAppObject } from './app-object.model';

export const sampleWithRequiredData: IAppObject = {
  id: 71011,
  code: 'multi-byte Salad Account',
  name: 'orchestration',
};

export const sampleWithPartialData: IAppObject = {
  id: 88796,
  code: 'sexy',
  name: 'white',
  objectType: AppObjectType['FUNCTIONAL_MODULE'],
  seq: 29860,
  quantity: 39991,
  validUntil: dayjs('2023-02-06T22:19'),
};

export const sampleWithFullData: IAppObject = {
  id: 29972,
  code: 'Licensed deposit copying',
  name: 'open-source',
  description: '../fake-data/blob/hipster.txt',
  objectType: AppObjectType['REPORT'],
  lastChange: dayjs('2023-02-07T08:50'),
  seq: 79422,
  status: StandardRecordStatus['ACTIVE'],
  quantity: 61287,
  validFrom: dayjs('2023-02-06T16:36'),
  validUntil: dayjs('2023-02-07T12:17'),
  isValid: false,
  creationDate: dayjs('2023-02-07'),
};

export const sampleWithNewData: NewAppObject = {
  code: 'Manat',
  name: 'orange Sleek Chief',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
