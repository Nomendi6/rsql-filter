import { Component, input } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ProgressBarModule } from 'primeng/progressbar';

import SharedModule from 'app/shared/shared.module';
import { GarbageCollector } from 'app/admin/metrics/metrics.model';

@Component({
  standalone: true,
  selector: 'app-metrics-garbagecollector',
  templateUrl: './metrics-garbagecollector.component.html',
  imports: [SharedModule, TableModule, ProgressBarModule],
})
export class MetricsGarbageCollectorComponent {
  /**
   * object containing garbage collector related metrics
   */
  garbageCollectorMetrics = input<GarbageCollector>();

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  updating = input<boolean>();
}
