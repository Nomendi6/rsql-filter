import { Component } from '@angular/core';
import { AccordionModule } from 'primeng/accordion';

import SharedModule from 'app/shared/shared.module';
import UserManagement$D$1Component from '../../user-management-d-1/update/user-management-d-1.component';

@Component({
  standalone: true,
  selector: 'app-user-management-a',
  templateUrl: './user-management-a.component.html',
  imports: [SharedModule, AccordionModule, UserManagement$D$1Component],
})
export default class UserManagement$AComponent {}
// layout:1.0 | entity-management.component
