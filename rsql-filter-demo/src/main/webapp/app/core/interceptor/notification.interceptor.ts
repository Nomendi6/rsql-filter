import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { translationNotFoundMessage } from 'app/config/translation.config';

export type AlertType = 'success' | 'danger' | 'warning' | 'info';

export interface Alert {
  id?: number;
  type: AlertType;
  message?: string;
  translationKey?: string;
  translationParams?: Record<string, unknown>;
  timeout?: number;
  toast?: boolean;
  position?: string;
  close?: (alerts: Alert[]) => void;
}

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {
  private messageService = inject(MessageService);
  private translateService = inject(TranslateService);

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          let alert: string | null = null;
          let alertParams: string | null = null;

          for (const headerKey of event.headers.keys()) {
            if (headerKey.toLowerCase().endsWith('app-alert')) {
              alert = event.headers.get(headerKey);
            } else if (headerKey.toLowerCase().endsWith('app-params')) {
              alertParams = decodeURIComponent(event.headers.get(headerKey)!.replace(/\+/g, ' '));
            }
          }

          if (alert) {
            const translatedAlert = this.translateToPrimeNG({
              type: 'success',
              translationKey: alert,
              translationParams: { param: alertParams },
            });
            this.messageService.add(translatedAlert);
          }
        }
      }),
    );
  }

  private translateToPrimeNG(alert: Alert): ToastMessageOptions {
    if (alert.translationKey) {
      const translatedMessage = this.translateService.instant(alert.translationKey, alert.translationParams);
      if (translatedMessage !== `${translationNotFoundMessage}[${alert.translationKey}]`) {
        alert.message = translatedMessage;
      } else alert.message ??= alert.translationKey;
    }
    const translatedAlert: ToastMessageOptions = {
      severity: alert.type,
      summary: alert.message,
    };
    return translatedAlert;
  }
}
