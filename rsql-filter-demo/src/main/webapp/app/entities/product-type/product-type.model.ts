import dayjs from 'dayjs/esm';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';

export interface IProductType {
  id?: number;
  code?: string;
  name?: string;
  description?: string | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
}

export class ProductType implements IProductType {
  constructor(
    public id?: number,
    public code?: string,
    public name?: string,
    public description?: string | null,
    public seq?: number | null,
    public status?: StandardRecordStatus | null,
    public validFrom?: dayjs.Dayjs | null,
    public validUntil?: dayjs.Dayjs | null,
  ) {}
}

export function getProductTypeIdentifier(productType: IProductType): number | undefined {
  return productType.id;
}

export type NewProductType = Omit<IProductType, 'id'> & { id: null };
