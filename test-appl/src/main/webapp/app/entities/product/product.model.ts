import dayjs from 'dayjs/esm';
import { IProductType } from 'app/entities/product-type/product-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

export interface IProduct {
  id: number;
  code?: string | null;
  name?: string | null;
  description?: string | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
  tproduct?: Pick<IProductType, 'id' | 'name'> | null;
  parent?: Pick<IProduct, 'id' | 'name'> | null;
}

export type NewProduct = Omit<IProduct, 'id'> & { id: null };
