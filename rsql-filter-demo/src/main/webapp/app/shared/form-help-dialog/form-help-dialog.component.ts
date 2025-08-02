import { Component, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

import SharedModule from 'app/shared/shared.module';
import { FormHelpDialogService } from './form-help-dialog.service';

@Component({
  standalone: true,
  selector: 'app-form-help-dialog',
  templateUrl: './form-help-dialog.component.html',
  imports: [SharedModule, DialogModule, ButtonModule],
})
export class FormHelpDialogComponent {
  public formHelpService = inject(FormHelpDialogService);
}
