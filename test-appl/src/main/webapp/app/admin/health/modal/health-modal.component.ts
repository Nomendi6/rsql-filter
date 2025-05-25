import { Component, OnInit, inject } from '@angular/core';
import { TableModule } from 'primeng/table';
import { DynamicDialogConfig, DynamicDialogModule, DynamicDialogRef } from 'primeng/dynamicdialog';

import SharedModule from 'app/shared/shared.module';
import { HealthDetails, HealthKey } from '../health.model';

@Component({
  standalone: true,
  selector: 'app-health-modal',
  templateUrl: './health-modal.component.html',
  imports: [SharedModule, TableModule, DynamicDialogModule],
})
export class HealthModalComponent implements OnInit {
  public health?: { key: HealthKey; value: HealthDetails };
  public ref = inject(DynamicDialogRef);
  public config = inject(DynamicDialogConfig);

  public ngOnInit(): void {
    this.health = this.config.data?.health;
  }

  public readableValue(value: any): string {
    if (this.health?.key === 'diskSpace') {
      // Should display storage space in an human readable unit
      const val = value / 1073741824;
      if (val > 1) {
        return `${val.toFixed(2)} GB`;
      }
      return `${(value / 1048576).toFixed(2)} MB`;
    }

    if (typeof value === 'object') {
      return JSON.stringify(value);
    }
    return String(value);
  }

  public getProperties(obj: any): any[] {
    return Object.keys(obj).map(key => ({ key, value: obj[key] }));
  }
}
