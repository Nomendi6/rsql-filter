import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn, Routes } from '@angular/router';
import { map, of } from 'rxjs';

import { IUser } from './user-management.model';
import { UserManagementStoreService } from './store/user-management-store.service';
import UserManagementComponent from './list/user-management.component';

export const UserManagementResolve: ResolveFn<IUser | null> = (route: ActivatedRouteSnapshot) => {
  const login = route.paramMap.get('login');
  if (login) {
    return inject(UserManagementStoreService)
      .find(login)
      .pipe(map(res => res.body));
  }
  return of(null);
};

const userManagementRoute: Routes = [
  {
    path: '',
    component: UserManagementComponent,
    data: {
      defaultSort: 'id,asc',
    },
    resolve: {
      user: UserManagementResolve,
    },
  },
];

export default userManagementRoute;
