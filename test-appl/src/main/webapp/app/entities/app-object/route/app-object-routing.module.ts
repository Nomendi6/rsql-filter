import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { AppObjectComponent } from '../list/app-object.component';
import { AppObjectDetailComponent } from '../detail/app-object-detail.component';
import { AppObjectUpdateComponent } from '../update/app-object-update.component';
import { AppObjectRoutingResolveService } from './app-object-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const appObjectRoute: Routes = [
  {
    path: '',
    component: AppObjectComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppObjectDetailComponent,
    resolve: {
      appObject: AppObjectRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppObjectUpdateComponent,
    resolve: {
      appObject: AppObjectRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppObjectUpdateComponent,
    resolve: {
      appObject: AppObjectRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(appObjectRoute)],
  exports: [RouterModule],
})
export class AppObjectRoutingModule {}
