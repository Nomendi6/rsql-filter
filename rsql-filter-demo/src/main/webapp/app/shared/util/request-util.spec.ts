import { expect } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { andRsql, filterToRsql, getEnumFilter, getFilterValue, getTableSort } from './request-util';

describe('And RSQL', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('left and right', () => {
    const result = andRsql('left', 'right');
    expect(result).toBe('left and right');
  });

  it('left only', () => {
    const result = andRsql('left', undefined);
    expect(result).toBe('left');
  });

  it('right only', () => {
    const result = andRsql(undefined, 'right');
    expect(result).toBe('right');
  });
});

describe('getTableSort', () => {
  it('should return default sort', () => {
    const result = getTableSort(undefined, true);
    expect(result).toStrictEqual(['id,asc']);
  });
  it('should return asc sort', () => {
    const result = getTableSort([{ field: 'id', order: 1 }]);
    expect(result).toStrictEqual(['id,asc']);
  });
  it('should return desc sort', () => {
    const result = getTableSort([{ field: 'id', order: -1 }]);
    expect(result).toStrictEqual(['id,desc']);
  });
  it('should return 2 element sort', () => {
    const result = getTableSort([
      { field: 'id', order: 1 },
      { field: 'name', order: -1 },
    ]);
    expect(result).toStrictEqual(['id,asc', 'name,desc']);
  });
});

describe('filterToRsql without filterTypes', () => {
  // const result = filterToRsql();
  it('should filter number equals', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id==1001)');
  });
  it('should filter number >=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'gte',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id=ge=1001)');
  });
  it('should filter number <=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'lte',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id=le=1001)');
  });
  it('should filter number >', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'gt',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id=gt=1001)');
  });
  it('should filter number <', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'lt',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id=lt=1001)');
  });
  it('should filter number !=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(id=!1001)');
  });
  it('should filter string startsWith', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(code=*"AR*")');
  });
  it('should filter string endsWith', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'endsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(code=*"*AR")');
  });
  it('should filter string contains', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'contains',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(code=*"*AR*")');
  });
  it('should filter string equals', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(code=="AR")');
  });
  it('should filter string notEquals', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(code=!"AR")');
  });
  it('should filter boolean true', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      flag: [
        {
          value: true,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(flag==true)');
  });
  it('should filter boolean false', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      flag: [
        {
          value: false,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(flag==false)');
  });
  it('should filter date is', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'is',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum==#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date isNot', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'isNot',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum=!#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date before', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'before',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum=lt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date dateBefore', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'dateBefore',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum=lt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date after', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'after',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum=gt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date dateAfter', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'dateAfter',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, undefined);
    expect(result).toStrictEqual('(datum=gt=#2000-01-01T00:00:00.000Z#)');
  });
});
describe('filterToRsql with filterTypes', () => {
  it('should filter number equals', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id==1001)');
  });
  it('should filter number in', () => {
    const filter = {
      id: [
        {
          value: [1001, 1002, 1003],
          matchMode: 'in',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=in=(1001,1002,1003))');
  });
  it('should filter number >=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'gte',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=ge=1001)');
  });
  it('should filter number <=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'lte',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=le=1001)');
  });
  it('should filter number >', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'gt',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=gt=1001)');
  });
  it('should filter number <', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'lt',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=lt=1001)');
  });
  it('should filter number !=', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id=!1001)');
  });
  it('should filter string startsWith', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=*"AR*")');
  });
  it('should filter string startsWith 2 conditions', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'startsWith',
          operator: 'or',
        },
        {
          value: 'GC',
          matchMode: 'startsWith',
          operator: 'or',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=*"AR*" or code=*"GC*")');
  });
  it('should filter string endsWith', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'endsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=*"*AR")');
  });
  it('should filter string contains', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'contains',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=*"*AR*")');
  });
  it('should filter string equals', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=="AR")');
  });
  it('should filter string notEquals', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=!"AR")');
  });
  it('should filter string in', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: ['A1', 'A2', 'A3'],
          matchMode: 'in',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=in=("A1","A2","A3"))');
  });
  it('should filter boolean true', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      flag: [
        {
          value: true,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(flag==true)');
  });
  it('should filter boolean false', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      flag: [
        {
          value: false,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(flag==false)');
  });
  it('should filter date is', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'is',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum==#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date in', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: [datum, datum, datum],
          matchMode: 'in',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=in=(#2000-01-01T00:00:00.000Z#,#2000-01-01T00:00:00.000Z#,#2000-01-01T00:00:00.000Z#))');
  });
  it('should filter date isNot', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'isNot',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=!#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date before', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'before',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=lt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date dateBefore', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'dateBefore',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=lt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date after', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'after',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=gt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter date dateAfter', () => {
    const datum: Date = new Date('2000-01-01');
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      datum: [
        {
          value: datum,
          matchMode: 'dateAfter',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(datum=gt=#2000-01-01T00:00:00.000Z#)');
  });
  it('should filter enum in ACTIVE', () => {
    const statusActive: any = {
      value: 'ACTIVE',
      label: 'Active',
    };

    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: [statusActive],
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(status=in=(#ACTIVE#))');
  });
  it('should filter enum in NULL or ACTIVE', () => {
    const statusNull: any = {
      value: null,
      label: '',
    };
    const statusActive: any = {
      value: 'ACTIVE',
      label: 'Active',
    };

    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: [statusNull, statusActive],
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(status=in=(#ACTIVE#) or status==null)');
  });
  it('should filter enum in NULL or ACTIVE or NOT_ACTIVE', () => {
    const statusNull: any = {
      value: null,
      label: '',
    };
    const statusActive: any = {
      value: 'ACTIVE',
      label: 'Active',
    };
    const statusNotActive: any = {
      value: 'NOT_ACTIVE',
      label: 'Not Active',
    };

    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: [statusNull, statusActive, statusNotActive],
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(status=in=(#ACTIVE#,#NOT_ACTIVE#) or status==null)');
  });
  it('should filter enum in NULL', () => {
    const statusNull: any = {
      value: null,
      label: '',
    };
    // const statusActive: any = {
    //   value: 'ACTIVE',
    //   label: 'Active',
    // };
    // const statusNotActive: any = {
    //   value: 'NOT_ACTIVE',
    //   label: 'Not Active',
    // };

    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: [statusNull],
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(status==null)');
  });
  it('should filter relationship equals', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AR',
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'relationship',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=="AR")');
  });
  it('should filter complex expression', () => {
    const statusNull: any = {
      value: null,
      label: '',
    };
    const statusActive: any = {
      value: 'ACTIVE',
      label: 'Active',
    };
    const statusNotActive: any = {
      value: 'NOT_ACTIVE',
      label: 'Not Active',
    };

    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: 'AA',
          matchMode: 'notEquals',
          operator: 'and',
        },
      ],
      status: [
        {
          value: [statusNull, statusActive, statusNotActive],
          matchMode: 'in',
          operator: 'and',
        },
      ],
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = undefined;
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id==1001) and (code=!"AA") and (status=in=(#ACTIVE#,#NOT_ACTIVE#) or status==null)');
  });
});
describe('global filters', () => {
  it('should filter global fields', () => {
    const filter = {
      id: [
        {
          value: null,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
      global: {
        value: 'CO',
        matchMode: 'contains',
      },
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      name: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = ['code', 'name'];
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(code=*"*CO*" or name=*"*CO*")');
  });
  it('should filter global and table filters', () => {
    const filter = {
      id: [
        {
          value: 1001,
          matchMode: 'equals',
          operator: 'and',
        },
      ],
      code: [
        {
          value: null,
          matchMode: 'startsWith',
          operator: 'and',
        },
      ],
      status: [
        {
          value: null,
          matchMode: 'in',
          operator: 'and',
        },
      ],
      global: {
        value: 'CO',
        matchMode: 'contains',
      },
    };
    const filterTypes = {
      id: 'number',
      code: 'string',
      name: 'string',
      flag: 'boolean',
      datum: 'date',
      status: 'enum',
    };
    const globalFilterFields = ['code', 'name'];
    const result = filterToRsql(filter, globalFilterFields, filterTypes);
    expect(result).toStrictEqual('(id==1001) and (code=*"*CO*" or name=*"*CO*")');
  });
});
describe('getFilterValue', () => {
  it('should return null when value is null', () => {
    const result = getFilterValue(null, 'equal', 'string');
    expect(result).toBe('');
  });
});
describe('getEnumFilter', () => {
  it('should return null when value is null', () => {
    const result = getEnumFilter('fieldName', null, 'equal', '==');
    expect(result).toBe('');
  });
  it('should return one value', () => {
    const result = getEnumFilter('fieldName', { value: 'VALUE', label: 'Label' }, 'equal', '==');
    expect(result).toBe('fieldName==#VALUE#');
  });
});
