import dayjs from 'dayjs/esm';
import { AppObjectType } from 'app/entities/enumerations/app-object-type/app-object-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';

export interface IAppObject {
  id?: number;
  code?: string;
  name?: string;
  description?: string | null;
  objectType?: AppObjectType | null;
  lastChange?: dayjs.Dayjs | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  quantity?: number | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
  parent?: IAppObject | null;
}

export class AppObject implements IAppObject {
  constructor(
    public id?: number,
    public code?: string,
    public name?: string,
    public description?: string | null,
    public objectType?: AppObjectType | null,
    public lastChange?: dayjs.Dayjs | null,
    public seq?: number | null,
    public status?: StandardRecordStatus | null,
    public quantity?: number | null,
    public validFrom?: dayjs.Dayjs | null,
    public validUntil?: dayjs.Dayjs | null,
    public parent?: IAppObject | null,
  ) {}
}

export function getAppObjectIdentifier(appObject: IAppObject): number | undefined {
  return appObject.id;
}

export type NewAppObject = Omit<IAppObject, 'id'> & { id: null };
