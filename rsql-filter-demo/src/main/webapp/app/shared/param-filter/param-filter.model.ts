export type IParamFilterForm = Record<
  string,
  Partial<{
    fieldType: AvailableFieldType | null;
    filterType: string | null;
    value: string | number | string[] | boolean | Date | IObjectFilterValue | IObjectFilterValue[] | null;
    value2: number | null;
    searchFields: string[] | null;
    primaryKey: string | null;
  }>
>;

export type AvailableFieldType = 'field' | 'enum' | 'relationship';

export type ParamFilterFormEntry = {
  [K in keyof IParamFilterForm]: { key: K; value: IParamFilterForm[K] };
}[keyof IParamFilterForm];

export interface IObjectFilterValue {
  id?: string | number;
  [key: string]: any;
}
