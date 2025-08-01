import dayjs from 'dayjs/esm';
import { IProductType } from 'app/entities/product-type/product-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status/standard-record-status.model';

export interface IProduct {
  id?: number;
  code?: string;
  name?: string;
  description?: string | null;
  seq?: number | null;
  status?: StandardRecordStatus | null;
  validFrom?: dayjs.Dayjs | null;
  validUntil?: dayjs.Dayjs | null;
  tproduct?: IProductType | null;
  parent?: IProduct | null;
}

export class Product implements IProduct {
  constructor(
    public id?: number,
    public code?: string,
    public name?: string,
    public description?: string | null,
    public seq?: number | null,
    public status?: StandardRecordStatus | null,
    public validFrom?: dayjs.Dayjs | null,
    public validUntil?: dayjs.Dayjs | null,
    public tproduct?: IProductType | null,
    public parent?: IProduct | null,
  ) {}
}

export function getProductIdentifier(product: IProduct): number | undefined {
  return product.id;
}

export type NewProduct = Omit<IProduct, 'id'> & { id: null };
