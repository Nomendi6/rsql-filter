import { FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

import { IValueWithLabel } from '../common-types/value-with-label.model';
import { IExtendedFilter } from '../extended-filter/extended-filter.model';
import { filterMatchModeOptions } from '../extended-filter/filter-match-mode.model';
import { IParamFilterForm, ParamFilterFormEntry } from './param-filter.model';

export abstract class BaseParamFilter {
  public availableStringFilterTypes: IValueWithLabel[] = [];
  public availableNumberFilterTypes: IValueWithLabel[] = [];
  public availableDateFilterTypes: IValueWithLabel[] = [];

  constructor(protected translateService: TranslateService) {
    this.loadFilterMatchModeOptions();

    this.translateService.onLangChange.subscribe(() => {
      this.loadFilterMatchModeOptions();
    });
  }

  public isValue2VisibleForFormGroup(formGroup: FormGroup): boolean {
    const filterType = formGroup.get('filterType')?.value;
    const isValue2Visible = filterType === 'between';

    if (!isValue2Visible) {
      formGroup.get('value2')?.setValue(null);
      formGroup.get('value2')?.disable();
    } else {
      formGroup.get('value2')?.enable();
    }

    return isValue2Visible;
  }

  public mergeTableFilterWithParamFilter(
    paramFilter: Record<string, IExtendedFilter[]>,
    tableFilter: Record<string, IExtendedFilter[]>,
  ): Record<string, IExtendedFilter[]> {
    // Param filter is the base one and should not be overridden by table filter
    const mergedFilter = { ...paramFilter };

    Object.entries(tableFilter).forEach(([key, value]) => {
      if (key in mergedFilter) {
        mergedFilter[key] = [...mergedFilter[key], ...value];
        return;
      }
      mergedFilter[key] = value;
    });

    return mergedFilter;
  }

  protected translateToExtendedFilter(filterForm: IParamFilterForm): Record<string, IExtendedFilter[] | undefined> {
    const extendedFilter: Record<string, IExtendedFilter[] | undefined> = {};

    Object.entries(filterForm).forEach(([key, value]) => {
      const fieldType = value.fieldType ?? 'field';
      const filterValue = value.value;

      switch (fieldType) {
        case 'enum':
          this.buildExtendedFilterForEnum({ key, value }, extendedFilter);
          break;
        case 'relationship':
          this.buildExtendedFilterForRelationship({ key, value }, extendedFilter);
          break;
        default:
          this.buildExtendedFilterForField({ key, value }, extendedFilter);
          break;
      }

      if ((filterValue !== false && !filterValue) || (filterValue && Array.isArray(filterValue) && filterValue.length === 0)) {
        const fieldKey = fieldType === 'relationship' ? `${key}.${value.primaryKey ?? 'id'}` : key;
        extendedFilter[fieldKey] = extendedFilter[fieldKey]?.map(filter => ({ ...filter, value: null }));
      }
    });

    return extendedFilter;
  }

  public getDefaultFilterTypeForFieldType(formEntry: ParamFilterFormEntry): string {
    const fieldValue = formEntry.value.value;

    if (!fieldValue || typeof fieldValue === 'number') {
      return 'equals';
    }

    if (typeof fieldValue === 'string') {
      return 'startsWith';
    }

    if (fieldValue instanceof Date) {
      return 'dateIs';
    }

    return 'in';
  }

  private buildExtendedFilterForField(
    formEntry: ParamFilterFormEntry,
    extendedFilter: Record<string, IExtendedFilter[] | undefined>,
  ): Record<string, IExtendedFilter[] | undefined> {
    const filterType = formEntry.value.filterType ?? this.getDefaultFilterTypeForFieldType(formEntry);
    const filterValue = formEntry.value.value;
    const filterValue2 = formEntry.value.value2;
    const key = formEntry.key;

    if (filterValue2) {
      extendedFilter[key] = [
        {
          value: filterValue,
          matchMode: 'gt',
          operator: 'and',
        },
        {
          value: filterValue2,
          matchMode: 'lt',
          operator: 'and',
        },
      ];
      return extendedFilter;
    }

    extendedFilter[key] = [
      {
        value: filterValue,
        matchMode: filterType,
        operator: 'and',
      },
    ];

    return extendedFilter;
  }

  private buildExtendedFilterForEnum(
    formEntry: ParamFilterFormEntry,
    extendedFilter: Record<string, IExtendedFilter[] | undefined>,
  ): Record<string, IExtendedFilter[] | undefined> {
    const filterType = formEntry.value.filterType ?? 'in';
    const filterValue = formEntry.value.value;
    const key = formEntry.key;

    extendedFilter[key] = [
      {
        value: filterValue,
        matchMode: filterType,
        operator: 'and',
      },
    ];

    return extendedFilter;
  }

  private buildExtendedFilterForRelationship(
    formEntry: ParamFilterFormEntry,
    extendedFilter: Record<string, IExtendedFilter[] | undefined>,
  ): Record<string, IExtendedFilter[] | undefined> {
    const filterType = formEntry.value.filterType ?? this.getDefaultFilterTypeForFieldType(formEntry);
    const filterValue = formEntry.value.value;
    const searchFields = formEntry.value.searchFields ?? [];
    const key = formEntry.key;
    const primaryKey = formEntry.value.primaryKey ?? 'id';

    // If search fields are not empty, it means we are searching using text input
    if (searchFields.length > 0) {
      extendedFilter[key] = [
        {
          value: filterValue,
          matchMode: filterType,
          fields: searchFields,
          operator: 'or',
        },
      ];
      return extendedFilter;
    }

    // If search fields are empty, it means we are using either multiselect or autocomplete
    // for filtering in which case filterValue is either an object or an array of objects
    if (Array.isArray(filterValue) && filterValue.length > 0) {
      const filterValues: string[] = [];
      filterValue.forEach(item => {
        if (typeof item !== 'string') {
          const filterValueToUse = item[primaryKey] ?? item.id;
          if (filterValueToUse) {
            filterValues.push(filterValueToUse);
          }
        }
      });
      extendedFilter[`${key}.${primaryKey}`] = [
        {
          value: filterValues,
          matchMode: filterType,
          operator: 'or',
        },
      ];
      return extendedFilter;
    }

    // If filterValue is of string or number, it indicates that autocomplete only returns the ID of the related entity
    if (typeof filterValue === 'string' || typeof filterValue === 'number') {
      extendedFilter[`${key}.${primaryKey}`] = [
        {
          value: filterValue,
          matchMode: filterType,
        },
      ];
      return extendedFilter;
    }

    // Default case is that filterValue is an object and for filtering it we'll use the ID of the related entity
    if (typeof filterValue === 'object' && !Array.isArray(filterValue) && filterValue !== null && !(filterValue instanceof Date)) {
      const filterValueToUse = filterValue[primaryKey] ?? filterValue.id;
      if (filterValueToUse) {
        extendedFilter[`${key}.${primaryKey}`] = [
          {
            value: filterValueToUse,
            matchMode: filterType,
          },
        ];
      }
    }

    return extendedFilter;
  }

  private loadFilterMatchModeOptions(): void {
    this.availableStringFilterTypes = filterMatchModeOptions.text.map(option => ({
      value: option,
      label: this.translateService.instant(`form.filters.${option}`),
    }));

    this.availableNumberFilterTypes = filterMatchModeOptions.numeric.map(option => ({
      value: option,
      label: this.translateService.instant(`form.filters.${option}`),
    }));

    this.availableDateFilterTypes = filterMatchModeOptions.date.map(option => ({
      value: option,
      label: this.translateService.instant(`form.filters.${option}`),
    }));
  }
}
