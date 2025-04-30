import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class NameFormattingService {
  formatName(formGroup: FormGroup, fieldPrefix: string): void {
    const firstNameControlName = `${fieldPrefix}firstName`;
    const lastNameControlName = `${fieldPrefix}lastName`;
    const nameControlName = `${fieldPrefix}name`;

    const firstName: string = formGroup.controls[firstNameControlName].value ?? '';
    const lastName: string = formGroup.controls[lastNameControlName].value ?? '';
    const formattedName = `${firstName.trim()} ${lastName.trim()}`.trim();
    const nameControl = formGroup.get(nameControlName);
    if (nameControl) {
      nameControl.setValue(formattedName);
    }
  }
}
