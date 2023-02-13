import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IProductType } from '../product-type.model';
import { ProductTypeService } from '../service/product-type.service';

@Injectable({ providedIn: 'root' })
export class ProductTypeRoutingResolveService implements Resolve<IProductType | null> {
  constructor(protected service: ProductTypeService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IProductType | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((productType: HttpResponse<IProductType>) => {
          if (productType.body) {
            return of(productType.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(null);
  }
}
