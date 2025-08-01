import { expect, jest } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { SecurityContext } from '@angular/core';
import { FormHelpDialogService } from './form-help-dialog.service';

describe('FormHelpDialogService', () => {
  let service: FormHelpDialogService;
  let sanitizer: DomSanitizer;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FormHelpDialogService,
        {
          provide: DomSanitizer,
          useValue: {
            sanitize: jest.fn(),
            bypassSecurityTrustHtml: jest.fn(),
          },
        },
      ],
    });

    service = TestBed.inject(FormHelpDialogService);
    sanitizer = TestBed.inject(DomSanitizer);
  });

  describe('getDialogContent$ method', () => {
    it('should return the formHelp Observable', () => {
      expect(service.getDialogContent$()).toBeInstanceOf(Observable);
    });
  });

  describe('closeDialog method', () => {
    it('should set the formHelp BehaviorSubject to null', () => {
      service.closeDialog();
      expect((service as any).formHelp.value).toBeNull();
    });
  });

  describe('showHelpForForm method', () => {
    it('should set the form into the dialog with the expected content', () => {
      const formName = 'test-form-name';
      const formHelpPath = `content/docs/forms/${formName}.html`;
      const trustedFormHelpPath = 'test-trusted-form-help-path';
      const trustedHtml = 'test-trusted-html';

      jest.spyOn(sanitizer, 'sanitize').mockReturnValue(trustedFormHelpPath);
      jest.spyOn(sanitizer, 'bypassSecurityTrustHtml').mockReturnValue(trustedHtml);

      service.showHelpForForm(formName);

      expect(sanitizer.sanitize).toHaveBeenCalledWith(SecurityContext.HTML, formHelpPath);
      expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalledWith(
        `<iframe sandbox="allow-popups" src="${trustedFormHelpPath}" sandbox width="100%" height="100%"></iframe>`,
      );
    });

    it("should set the iframe's src to empty if the sanitize functions returns null", () => {
      const formName = 'test-form-name';
      const formHelpPath = `content/docs/forms/${formName}.html`;
      const trustedHtml = 'test-trusted-html';

      jest.spyOn(sanitizer, 'sanitize').mockReturnValue(null);
      jest.spyOn(sanitizer, 'bypassSecurityTrustHtml').mockReturnValue(trustedHtml);

      service.showHelpForForm(formName);

      expect(sanitizer.sanitize).toHaveBeenCalledWith(SecurityContext.HTML, formHelpPath);
      expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalledWith(
        `<iframe sandbox="allow-popups" src="" sandbox width="100%" height="100%"></iframe>`,
      );
    });
  });

  describe('openInNewTab method', () => {
    it('should open a new tab with the form help documentation URL', () => {
      const docsUrl = 'test-docs-url';

      jest.spyOn(window, 'open').mockImplementation(() => null);

      (service as any).formHelp.next({
        name: 'test-form',
        docsUrl,
        content: 'test-content',
      });

      service.openInNewTab();

      expect(window.open).toHaveBeenCalledWith(docsUrl, '_blank');
    });
  });
});
