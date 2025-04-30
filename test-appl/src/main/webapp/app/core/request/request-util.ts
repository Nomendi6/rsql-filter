import { HttpParams } from '@angular/common/http';

export const createRequestOption = (req?: any, addSortById = false): HttpParams => {
  let options: HttpParams = new HttpParams();

  if (req) {
    Object.keys(req).forEach(key => {
      if (key !== 'sort' && req[key] && req[key] !== '' && key !== 'filter') {
        options = options.set(key, req[key]);
      }
    });

    if (req.sort) {
      req.sort.forEach((val: string) => {
        options = options.append('sort', val);
      });
    }
    if (req.filter && req.filter > '') {
      options = options.set('filter', req.filter);
    }
    if (!(req.sort && req.sort.length > 0) && addSortById) {
      options = options.append('sort', 'id,asc');
    }
  }

  return options;
};
