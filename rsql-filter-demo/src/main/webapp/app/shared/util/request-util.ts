import { SortMeta } from 'primeng/api';
import { IExtendedFilter } from 'app/shared/extended-filter/extended-filter.model';

// export { filterToRsql, getTableSort, andRsql, getFilterValue, getFilterValue2 };
export { filterToRsql, getTableSort, andRsql, getFilterValue, getEnumFilter };

const mapRsqlOperator: Record<string, string> = {
  startsWith: '=*',
  contains: '=*',
  notContains: '!=*',
  endsWith: '=*',
  equals: '==',
  notEquals: '=!',
  in: '=in=',
  lt: '=lt=',
  lte: '=le=',
  gt: '=gt=',
  gte: '=ge=',
  is: '==',
  isNot: '=!',
  before: '=lt=',
  after: '=gt=',
  dateBefore: '=lt=',
  dateAfter: '=gt=',
  dateIs: '==',
  dateIsNot: '=!',
};

// for old rsql implementation
/*
function getFilterValue(value: any, operatorName: string | undefined): string {
  if (!value) {
    return 'null';
  }
  if (typeof value === 'object') {
    if (value instanceof Date) {
      return value.toISOString();
    }
  } else {
    switch (typeof value) {
      case 'string': {
        if (operatorName) {
          switch (operatorName) {
            case 'startsWith':
              value = value + '*';
              break;
            case 'endsWith':
              value = '*' + value;
              break;
            case 'contains':
              value = '*' + value + '*';
          }
        }
        return "'" + (value as string) + "'";
      }
      case 'number': {
        return value.toString();
      }
      case 'boolean': {
        return value ? 'true' : 'false';
      }
    }

    return value as string;
  }
  return '';
}
*/

function getFilterValue(value: any, operatorName?: string, givenFieldType?: string): string {
  if (value === null || value === undefined) {
    return '';
  }
  let fieldType: string;
  let valueType: string = typeof value;
  let isArray = false;

  if (typeof value === 'object') {
    if (value instanceof Date) {
      valueType = 'date';
    } else if (value instanceof Array) {
      isArray = true;
      if (value.length > 0) {
        valueType = typeof value[0];
      }
    }
  }

  if (!givenFieldType) {
    fieldType = valueType;
  } else {
    fieldType = givenFieldType;
    if (givenFieldType === 'relationship') {
      fieldType = valueType;
    }
  }

  if (fieldType === 'date') {
    if (isArray) {
      const content: string = value.map((e: Date | string) => `#${new Date(e).toISOString()}#`).join(',');
      return `(${content})`;
    }
    return `#${new Date(value).toISOString()}#`;
  }
  switch (fieldType) {
    case 'string': {
      if (operatorName) {
        switch (operatorName) {
          case 'startsWith':
            return `"${value as string}*"`;
          case 'endsWith':
            return `"*${value as string}"`;
          case 'contains':
            return `"*${value as string}*"`;
          case 'notContains':
            return `"*${value as string}*"`;
          case 'in': {
            const content: string = value.map((e: string) => `"${e}"`).join(',');
            return `(${content})`;
          }
        }
      }
      return `"${value as string}"`;
    }
    case 'number': {
      if (isArray) {
        const content: string = value.map((e: number) => e.toString()).join(',');
        return `(${content})`;
      }
      return (value as number).toString();
    }
    case 'boolean': {
      return value ? 'true' : 'false';
    }
  }

  return value as string;
}

function getEnumFilter(fieldName: string, value: any, operatorName: string | undefined, rsqlOperator: string): string {
  if (value === null || value === undefined) {
    return '';
  }

  let isArray = false;

  if (typeof value === 'object') {
    if (value instanceof Array) {
      isArray = true;

      // Enums are expected to be in { value, label } format
      // We need to transform the value if necessary
      value.forEach((item, index) => {
        if (typeof item === 'string') {
          value[index] = {
            value: item,
            label: item,
          };
        }
      });
    }
  }

  if (isArray) {
    if (value.length === 0) {
      return '';
    }

    let hasNull = false;
    let content = '';
    const v: { value: string | undefined | null; label: string }[] = value;

    for (const item of v) {
      if (item.value) {
        const expression = `#${item.value}#`;
        if (content.length > 0) {
          content += `,${expression}`;
        } else {
          content = expression;
        }
      } else {
        hasNull = true;
      }
    }

    if (hasNull) {
      if (content.length > 0) {
        return `${fieldName}${rsqlOperator}(${content}) or ${fieldName}==null`;
      }
      return `${fieldName}==null`;
    }
    return `${fieldName}${rsqlOperator}(${content})`;
  }
  const v: { value: string | undefined | null; label: string } = value;
  return `${fieldName}${rsqlOperator}#${v.value as string}#`;
}

function filterToRsql(
  filter: Record<string, IExtendedFilter | IExtendedFilter[]>,
  globalFilterFields?: string[],
  filterTypes: Record<string, string | undefined> = {},
): string {
  let result = '';
  for (const fieldName in filter) {
    if (fieldName !== 'global') {
      const fields = filter[fieldName];
      if (Array.isArray(fields) && fields.length > 0) {
        let fieldFilter = '';
        const fieldFilterType = filterTypes[fieldName];
        fields.forEach(field => {
          if (field.value !== null && field.value !== undefined) {
            const operatorName = field.matchMode ?? 'startsWith';
            const additionOperator = field.operator ?? 'or';
            const searchFields = field.fields ?? [];
            let rsqlOperator = mapRsqlOperator[operatorName];
            if (fieldFilterType === 'boolean') {
              rsqlOperator = '==';
            }
            let fieldValue: string;
            let expression: string;

            if (searchFields.length > 0) {
              let searchFieldExpression = '';

              searchFields.forEach(searchField => {
                fieldValue = getFilterValue(field.value, operatorName, fieldFilterType);
                expression = `${fieldName}.${searchField}${rsqlOperator}${fieldValue}`;

                if (searchFieldExpression.length > 0) {
                  searchFieldExpression = `${searchFieldExpression} or ${expression}`;
                } else {
                  searchFieldExpression = expression;
                }
              });

              expression = searchFieldExpression;

              if (fieldFilter.length > 0) {
                fieldFilter = `${fieldFilter} ${additionOperator} ${expression}`;
              } else {
                fieldFilter = expression;
              }

              return;
            }

            if (fieldFilterType === 'enum') {
              expression = getEnumFilter(fieldName, field.value, operatorName, rsqlOperator);
            } else {
              fieldValue = getFilterValue(field.value, operatorName, fieldFilterType);
              expression = `${fieldName}${rsqlOperator}${fieldValue}`;
            }

            if (fieldFilter.length > 0) {
              fieldFilter = `${fieldFilter} ${additionOperator} ${expression}`;
            } else {
              fieldFilter = expression;
            }
          }
        });
        if (fieldFilter.length > 0) {
          if (result.length > 0) {
            result = `${result} and (${fieldFilter})`;
          } else {
            result = `(${fieldFilter})`;
          }
        }
      }
    } else {
      // handle global
      const field = filter.global;
      if (!Array.isArray(field) && globalFilterFields && globalFilterFields.length > 0 && field.value) {
        const operatorName = field.matchMode ?? 'startsWith';
        let rsqlOperator = mapRsqlOperator[operatorName];
        const fieldFilterType = filterTypes[fieldName];
        if (fieldFilterType === 'boolean') {
          rsqlOperator = '==';
        }
        const fieldValue = getFilterValue(field.value, operatorName);
        const additionOperator = 'or';
        let fieldFilter = '';
        globalFilterFields.forEach(globalFieldName => {
          if (fieldFilter.length > 0) {
            fieldFilter = `${fieldFilter} ${additionOperator} ${globalFieldName}${rsqlOperator}${fieldValue}`;
          } else {
            fieldFilter = `${globalFieldName}${rsqlOperator}${fieldValue}`;
          }
        });
        if (fieldFilter.length > 0) {
          if (result.length > 0) {
            result = `${result} and (${fieldFilter})`;
          } else {
            result = `(${fieldFilter})`;
          }
        }
      }
    }
  }
  return result;
}

function getTableSort(tableSort: SortMeta[] | null | undefined, defaultOrder = true): string[] {
  if (!tableSort) {
    return defaultOrder ? ['id,asc'] : [];
  }
  const sort: string[] = [];
  tableSort.forEach(({ field, order }) => {
    if (order > 0) {
      sort.push(`${field},asc`);
    } else {
      sort.push(`${field},desc`);
    }
  });
  return sort;
}

function andRsql(leftExpression: string | undefined, rightExpression: string | undefined): string {
  let result: string;
  leftExpression = leftExpression ?? '';
  result = leftExpression.trim();
  rightExpression = rightExpression ?? '';
  rightExpression = rightExpression.trim();
  if (rightExpression.length > 0) {
    if (result.length > 0) {
      result = `${result} and ${rightExpression}`;
    } else {
      result = rightExpression;
    }
  }

  return result;
}
