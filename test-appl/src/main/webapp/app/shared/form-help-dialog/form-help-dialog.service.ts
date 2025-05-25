import { Injectable, SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BehaviorSubject, Observable } from 'rxjs';
import { IFormDocumentation } from './form-help.model';

@Injectable({
  providedIn: 'root',
})
export class FormHelpDialogService {
  private formHelp = new BehaviorSubject<IFormDocumentation | null>(null);

  constructor(private sanitizer: DomSanitizer) {}

  public getDialogContent$(): Observable<IFormDocumentation | null> {
    return this.formHelp.asObservable();
  }

  public closeDialog(): void {
    this.formHelp.next(null);
  }

  public showHelpForForm(formName: string): void {
    const formHelpPath = this.getFormHelpPath(formName);

    const trustedFormHelpPath = this.sanitizer.sanitize(SecurityContext.HTML, formHelpPath);

    const untrustedHtml = `<iframe sandbox="allow-popups" src="${trustedFormHelpPath ?? ''}" sandbox width="100%" height="100%"></iframe>`;
    const trustedHtml = this.sanitizer.bypassSecurityTrustHtml(untrustedHtml); // NOSONAR

    this.setFormIntoDialog({
      name: formName,
      docsUrl: formHelpPath,
      content: trustedHtml,
    });
  }

  public openInNewTab(): void {
    const formHelpPath = this.formHelp.value?.docsUrl;
    window.open(formHelpPath, '_blank');
  }

  private setFormIntoDialog(form: IFormDocumentation): void {
    this.formHelp.next(form);
  }

  private getFormHelpPath(formName: string): string {
    return `content/docs/forms/${formName}.html`;
  }
}
