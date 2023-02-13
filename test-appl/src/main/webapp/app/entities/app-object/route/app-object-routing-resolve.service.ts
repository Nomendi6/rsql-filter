import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAppObject } from '../app-object.model';
import { AppObjectService } from '../service/app-object.service';

@Injectable({ providedIn: 'root' })
export class AppObjectRoutingResolveService implements Resolve<IAppObject | null> {
  constructor(protected service: AppObjectService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IAppObject | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((appObject: HttpResponse<IAppObject>) => {
          if (appObject.body) {
            return of(appObject.body);
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
