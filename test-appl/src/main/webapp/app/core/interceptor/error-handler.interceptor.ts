import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { translationNotFoundMessage } from 'app/config/translation.config';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {
  private readonly eventManager = inject(EventManager);
  private readonly messageService = inject(MessageService);
  private readonly translateService = inject(TranslateService);

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap({
        error: (err: HttpErrorResponse) => {
          if (!(err.status === 401 && (err.message === '' || err.url?.includes('api/account')))) {
            this.eventManager.broadcast(new EventWithContent('testapplApp.httpError', err));
            this.handleError(err);
          }
        },
      }),
    );
  }

  private handleError(errorResponse: HttpErrorResponse): void {
    switch (errorResponse.status) {
      // Connection refused, server not reachable
      case 0:
        this.addAlert('Server not reachable', 'error.server.not.reachable');
        break;

      case 400: {
        const arr = errorResponse.headers.keys();
        let errorHeader: string | null = null;
        let entityKey: string | null = null;
        for (const entry of arr) {
          if (entry.toLowerCase().endsWith('app-error')) {
            errorHeader = errorResponse.headers.get(entry) ?? null;
          } else if (entry.toLowerCase().endsWith('app-params')) {
            entityKey = errorResponse.headers.get(entry) ?? null;
          }
        }
        if (errorHeader) {
          const alertData = entityKey ? { entityName: this.translateService.instant(`global.menu.entities.${entityKey}`) } : undefined;
          this.addAlert(errorHeader, errorHeader, alertData);
        } else if (errorResponse.error !== '' && errorResponse.error.fieldErrors) {
          const fieldErrors = errorResponse.error.fieldErrors;
          for (const fieldError of fieldErrors) {
            if (['Min', 'Max', 'DecimalMin', 'DecimalMax'].includes(fieldError.message as string)) {
              fieldError.message = 'Size';
            }
            // convert 'something[14].other[4].id' to 'something[].other[].id' so translations can be written to it
            const convertedField: string = fieldError.field.replace(/\[\d*\]/g, '[]');
            const fieldName: string = this.translateService.instant(`aportalApp.${fieldError.objectName as string}.${convertedField}`);
            this.addAlert(`Error on field "${fieldName}"`, `error.${fieldError.message as string}`, { fieldName });
          }
        } else if (errorResponse.error !== '' && errorResponse.error?.message) {
          this.addAlert(
            (errorResponse.error.detail ?? errorResponse.error.message) as string,
            errorResponse.error.message as string,
            errorResponse.error.params as Record<string, unknown>,
          );
        } else {
          this.addAlert(errorResponse.error as string, errorResponse.error as string);
        }
        break;
      }

      case 404:
        this.addAlert('Not found', 'error.url.not.found');
        break;

      case 403:
        this.addAlert('Forbidden', 'error.http.403');
        break;

      default:
        if (errorResponse.error !== '' && errorResponse.error.message) {
          this.addAlert(
            (errorResponse.error.detail ?? errorResponse.error.message) as string,
            errorResponse.error.message as string,
            errorResponse.error.params as Record<string, unknown>,
          );
        } else {
          this.addAlert(errorResponse.error, errorResponse.error);
        }
    }
  }

  private addAlert(message?: string, translationKey?: string, translationParams?: Record<string, unknown>): ToastMessageOptions {
    if (translationKey) {
      const translatedMessage = this.translateService.instant(translationKey, translationParams);
      if (translatedMessage !== `${translationNotFoundMessage}[${translationKey}]`) {
        message = translatedMessage;
      } else message ??= translationKey;
    }
    const primengAlert: ToastMessageOptions = {
      severity: 'error',
      summary: message,
    };
    this.messageService.add(primengAlert);
    return primengAlert;
  }
}
