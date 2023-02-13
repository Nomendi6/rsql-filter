import dayjs from 'dayjs/esm';
import { AppObjectType } from 'app/entities/enumerations/app-object-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

export interface IAppObject {
  id: number;
  code?: string | null;
  name?: string | null;
  description?: string | null;
  objectType?: AppObjectType | null;
  lastChange?: dayjs.Dayjs | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  quantity?: number | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
  isValid?: boolean | null;
  creationDate?: dayjs.Dayjs | null;
  parent?: Pick<IAppObject, 'id' | 'name'> | null;
}

export type NewAppObject = Omit<IAppObject, 'id'> & { id: null };
