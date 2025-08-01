import { AfterViewInit, Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import SharedModule from 'app/shared/shared.module';

import { PasswordResetInitService } from './password-reset-init.service';

@Component({
  standalone: true,
  selector: 'app-password-reset-init',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, ButtonModule, InputTextModule],
  templateUrl: './password-reset-init.component.html',
})
export default class PasswordResetInitComponent implements AfterViewInit {
  public email = viewChild.required<ElementRef>('email');
  public success = signal(false);
  public resetRequestForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email]),
  });

  private passwordResetInitService = inject(PasswordResetInitService);

  public ngAfterViewInit(): void {
    this.email().nativeElement.focus();
  }

  public requestReset(): void {
    const email = this.resetRequestForm.get(['email'])!.value;
    this.passwordResetInitService.save(email).subscribe(() => this.success.set(true));
  }
}
