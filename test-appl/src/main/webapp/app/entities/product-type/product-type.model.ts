import dayjs from 'dayjs/esm';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

export interface IProductType {
  id: number;
  code?: string | null;
  name?: string | null;
  description?: string | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
}

export type NewProductType = Omit<IProductType, 'id'> & { id: null };
