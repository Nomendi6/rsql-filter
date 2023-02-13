import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { AppObjectComponent } from './list/app-object.component';
import { AppObjectDetailComponent } from './detail/app-object-detail.component';
import { AppObjectUpdateComponent } from './update/app-object-update.component';
import { AppObjectDeleteDialogComponent } from './delete/app-object-delete-dialog.component';
import { AppObjectRoutingModule } from './route/app-object-routing.module';

@NgModule({
  imports: [SharedModule, AppObjectRoutingModule],
  declarations: [AppObjectComponent, AppObjectDetailComponent, AppObjectUpdateComponent, AppObjectDeleteDialogComponent],
})
export class AppObjectModule {}
