import { Component } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ReactiveFormsModule } from '@angular/forms';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { CheckboxModule } from 'primeng/checkbox';
import { ButtonModule } from 'primeng/button';

import SharedModule from 'app/shared/shared.module';
import { UserManagementStoreService } from '../../store/user-management-store.service';
import { UserManagement$D$1FormGroupService } from '../../store/user-management-d-1-formgroup.service';
import { IUser } from '../../user-management.model';

@Component({
  standalone: true,
  selector: 'app-user-management-d-1',
  templateUrl: './user-management-d-1.component.html',
  imports: [SharedModule, ReactiveFormsModule, DropdownModule, InputTextModule, MultiSelectModule, CheckboxModule, ButtonModule],
  providers: [UserManagement$D$1FormGroupService],
})
export default class UserManagement$D$1Component {
  isSaving = false;

  constructor(
    public store: UserManagementStoreService,
    public fg: UserManagement$D$1FormGroupService,
  ) {}

  previousState(): void {
    this.fg.cancelEdit();
  }

  save(): void {
    this.isSaving = true;
    this.subscribeToSaveResponse(this.fg.save$());
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IUser>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    // this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }
}
// form | v1.0
