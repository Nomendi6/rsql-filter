import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { DialogService } from 'primeng/dynamicdialog';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { HealthService } from './health.service';
import { Health, HealthDetails, HealthStatus } from './health.model';
import { HealthModalComponent } from './modal/health-modal.component';

@Component({
  standalone: true,
  selector: 'app-health',
  templateUrl: './health.component.html',
  imports: [SharedModule, HealthModalComponent, ButtonModule, DialogModule],
})
export default class HealthComponent implements OnInit {
  public health?: Health;
  public displayHealthDialog = false;
  public loading = false;

  private dialogService = inject(DialogService);
  private healthService = inject(HealthService);
  private translateService = inject(TranslateService);

  public ngOnInit(): void {
    this.refresh();
  }

  public getBadgeClass(statusState: HealthStatus): string {
    if (statusState === 'UP') {
      return 'badge-success';
    }
    return 'badge-danger';
  }

  public refresh(): void {
    this.loading = true;
    this.healthService
      .checkHealth()
      .pipe(
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: health => (this.health = health),
        error: (error: HttpErrorResponse) => {
          if (error.status === 503) {
            this.health = error.error;
          }
        },
      });
  }

  public showHealth(health: { key: string; value: HealthDetails }): void {
    this.dialogService.open(HealthModalComponent, {
      data: {
        health,
      },
      header: this.translateService.instant(`health.indicator.${health.key}`),
      width: '70%',
    });
  }

  // getProps2(components: Record<string, HealthDetails>): HealthComponentStatus[] {
  //   const properties: HealthComponentStatus[] = [];
  //   for (const [key, value] of Object.entries(components)) {
  //      properties.push({ name: key, status: value.status, details: value.details });
  //   }
  //   return properties;
  // }
}
