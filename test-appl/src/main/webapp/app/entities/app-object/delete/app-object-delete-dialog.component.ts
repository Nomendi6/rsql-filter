import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IAppObject } from '../app-object.model';
import { AppObjectService } from '../service/app-object.service';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';

@Component({
  templateUrl: './app-object-delete-dialog.component.html',
})
export class AppObjectDeleteDialogComponent {
  appObject?: IAppObject;

  constructor(protected appObjectService: AppObjectService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appObjectService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
