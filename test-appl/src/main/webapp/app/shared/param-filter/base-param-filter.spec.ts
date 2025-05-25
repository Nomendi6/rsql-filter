import { expect, jest } from '@jest/globals';
import { FormControl, FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { EventEmitter } from '@angular/core';

import { IExtendedFilter } from '../extended-filter/extended-filter.model';
import { BaseParamFilter } from './base-param-filter';
import { AvailableFieldType, IParamFilterForm, ParamFilterFormEntry } from './param-filter.model';

class BaseParamFilterImpl extends BaseParamFilter {}

describe('BaseParamFilter', () => {
  let paramFilter: BaseParamFilterImpl;
  const mockedTranslateService: Partial<TranslateService> = {
    onLangChange: new EventEmitter(),
    instant: jest.fn(),
  };
  const mockedFormGroup = new FormGroup({
    id: new FormGroup({
      fieldType: new FormControl<AvailableFieldType>('field'),
      filterType: new FormControl('equals'),
      value: new FormControl<number | null>(null),
      value2: new FormControl<number | null>({
        value: null,
        disabled: true,
      }),
    }),
    seq: new FormGroup({
      fieldType: new FormControl<AvailableFieldType>('field'),
      filterType: new FormControl('startsWith'),
      value: new FormControl<string | null>(null),
    }),
    status: new FormGroup({
      fieldType: new FormControl<AvailableFieldType>('enum'),
      filterType: new FormControl(''),
      value: new FormControl<string[] | null>(null),
    }),
    validFrom: new FormGroup({
      fieldType: new FormControl<AvailableFieldType>('field'),
      filterType: new FormControl('dateIs'),
      value: new FormControl<Date | null>(null),
    }),
    customer: new FormGroup({
      fieldType: new FormControl<AvailableFieldType>('relationship'),
      filterType: new FormControl('in'),
      value: new FormControl<undefined | null>(null),
      primaryKey: new FormControl<string>('id'),
    }),
  });

  beforeEach(() => {
    paramFilter = new BaseParamFilterImpl(mockedTranslateService as TranslateService);
  });

  it('should create', () => {
    expect(paramFilter).toBeTruthy();
  });

  describe('isValue2VisibleForFormGroup', () => {
    let formGroup: typeof mockedFormGroup;

    beforeEach(() => {
      formGroup = mockedFormGroup;
    });

    it('should return false and disable value2 control when filterType is not "between"', () => {
      // GIVEN
      formGroup.controls.id.controls.filterType.setValue('equals');
      formGroup.controls.id.controls.value2.enable();

      // WHEN
      const result = paramFilter.isValue2VisibleForFormGroup(formGroup.controls.id);

      // THEN
      expect(result).toBeFalsy();
      expect(formGroup.controls.id.controls.value2.value).toBeNull();
      expect(formGroup.controls.id.controls.value2.disabled).toBeTruthy();
    });

    it('should return true and enable value2 control when filterType is "between"', () => {
      // GIVEN
      formGroup.controls.id.controls.value2.disable();
      formGroup.controls.id.controls.value.setValue(1);
      formGroup.controls.id.controls.filterType.setValue('between');

      // WHEN
      const result = paramFilter.isValue2VisibleForFormGroup(formGroup.controls.id);

      // THEN
      expect(result).toBeTruthy();
      expect(formGroup.controls.id.controls.value2.enabled).toBeTruthy();
    });
  });

  describe('mergeTableFilterWithParamFilter', () => {
    it('should properly merge tableFilter with paramFilter', () => {
      // GIVEN
      // field1 is in tableFilter, field2 is in both filters, and field3 is in paramFilter
      const paramFilterValue: Record<string, IExtendedFilter[]> = {
        field2: [{ value: 'value2_param', matchMode: 'equals', operator: 'and' }],
        field3: [{ value: 'value3_param', matchMode: 'contains', operator: 'and' }],
      };
      const tableFilterValue: Record<string, IExtendedFilter[]> = {
        field1: [{ value: 'value1_table', matchMode: 'equals', operator: 'and' }],
        field2: [{ value: 'value2_table', matchMode: 'contains', operator: 'and' }],
      };
      const expectedMergedFilter: Record<string, IExtendedFilter[]> = {
        field1: [{ value: 'value1_table', matchMode: 'equals', operator: 'and' }],
        field2: [
          { value: 'value2_param', matchMode: 'equals', operator: 'and' },
          { value: 'value2_table', matchMode: 'contains', operator: 'and' },
        ],
        field3: [{ value: 'value3_param', matchMode: 'contains', operator: 'and' }],
      };

      // WHEN
      const mergedFilter = paramFilter.mergeTableFilterWithParamFilter(paramFilterValue, tableFilterValue);

      // THEN
      expect(mergedFilter).toEqual(expectedMergedFilter);
    });
  });

  describe('translateToExtendedFilter', () => {
    it('should translate filterForm to extendedFilter', () => {
      // GIVEN
      const filterForm: IParamFilterForm = {
        field1: { fieldType: 'field', value: 'value1' },
        field2: { fieldType: 'enum', value: 'value2' },
        field3: {
          fieldType: 'relationship',
          value: {
            id: 'value3_id',
          },
          filterType: 'in',
        },
      };
      const expectedExtendedFilter = {
        field1: [{ value: 'value1', matchMode: 'startsWith', operator: 'and' }],
        field2: [{ value: 'value2', matchMode: 'in', operator: 'and' }],
        'field3.id': [{ value: 'value3_id', matchMode: 'in' }],
      };

      // WHEN
      const extendedFilter = (paramFilter as any).translateToExtendedFilter(filterForm);

      // THEN
      expect(extendedFilter).toEqual(expectedExtendedFilter);
    });

    it('should set value to null in extendedFilter when filterValue is falsy or an empty array', () => {
      // GIVEN
      const filterForm: IParamFilterForm = {
        field1: { fieldType: 'field', value: null },
        field2: { fieldType: 'enum', value: [] },
      };
      const expectedExtendedFilter: Record<string, IExtendedFilter[]> = {
        field1: [{ value: null, matchMode: 'equals', operator: 'and' }],
        field2: [{ value: null, matchMode: 'in', operator: 'and' }],
      };

      // WHEN
      const extendedFilter = (paramFilter as any).translateToExtendedFilter(filterForm);

      // THEN
      expect(extendedFilter).toEqual(expectedExtendedFilter);
    });
  });

  describe('getDefaultFilterTypeForFieldType', () => {
    it('should return "startsWith" for string field type', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = { key: 'key', value: { filterType: null, value: 'value' } };

      // WHEN
      const filterType = paramFilter.getDefaultFilterTypeForFieldType(formEntry);

      // THEN
      expect(filterType).toBe('startsWith');
    });

    it('should return "equals" for number field type', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = { key: 'key', value: { filterType: null, value: 123 } };

      // WHEN
      const filterType = paramFilter.getDefaultFilterTypeForFieldType(formEntry);

      // THEN
      expect(filterType).toBe('equals');
    });

    it('should return "dateIs" for Date field type', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = { key: 'key', value: { filterType: null, value: new Date() } };

      // WHEN
      const filterType = paramFilter.getDefaultFilterTypeForFieldType(formEntry);

      // THEN
      expect(filterType).toBe('dateIs');
    });

    it('should return "equals" for other field types if filterType and fieldValue are undefined', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = { key: 'key', value: { filterType: null, value: null } };

      // WHEN
      const filterType = paramFilter.getDefaultFilterTypeForFieldType(formEntry);

      // THEN
      expect(filterType).toBe('equals');
    });

    it('should return "in" for other field types if filterType and fieldValue are defined', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = {
        key: 'key',
        value: {
          filterType: 'something',
          value: {
            prop1: 'value1',
            prop2: 'value2',
          },
        },
      };

      // WHEN
      const filterType = paramFilter.getDefaultFilterTypeForFieldType(formEntry);

      // THEN
      expect(filterType).toBe('in');
    });
  });

  describe('buildExtendedFilterForField', () => {
    it('should build extended filter for field with filterValue and filterType', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = {
        key: 'field1',
        value: {
          fieldType: 'field',
          filterType: 'startsWith',
          value: 'value1',
        },
      };
      const extendedFilter: Record<string, IExtendedFilter[]> = {};

      // WHEN
      const result = (paramFilter as any).buildExtendedFilterForField(formEntry, extendedFilter);

      // THEN
      expect(result).toEqual({
        field1: [
          {
            value: 'value1',
            matchMode: 'startsWith',
            operator: 'and',
          },
        ],
      });
    });

    it('should build extended filter for field with filterValue, filterType, and filterValue2', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = {
        key: 'field2',
        value: {
          fieldType: 'field',
          filterType: 'between',
          value: 1,
          value2: 2,
        },
      };
      const extendedFilter: Record<string, IExtendedFilter[]> = {};

      // WHEN
      const result = (paramFilter as any).buildExtendedFilterForField(formEntry, extendedFilter);

      // THEN
      expect(result).toEqual({
        field2: [
          {
            value: 1,
            matchMode: 'gt',
            operator: 'and',
          },
          {
            value: 2,
            matchMode: 'lt',
            operator: 'and',
          },
        ],
      });
    });

    it('should build extended filter for field with default filterType when filterType is not provided', () => {
      // GIVEN
      const formEntry: ParamFilterFormEntry = {
        key: 'field3',
        value: {
          fieldType: 'field',
          value: 'value4',
        },
      };
      const extendedFilter: Record<string, IExtendedFilter[]> = {};

      // WHEN
      const result = (paramFilter as any).buildExtendedFilterForField(formEntry, extendedFilter);

      // THEN
      expect(result).toEqual({
        field3: [
          {
            value: 'value4',
            matchMode: 'startsWith',
            operator: 'and',
          },
        ],
      });
    });
  });
});
