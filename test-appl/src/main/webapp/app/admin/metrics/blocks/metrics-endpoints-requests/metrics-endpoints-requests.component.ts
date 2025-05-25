import { Component, input } from '@angular/core';
import { TableModule } from 'primeng/table';

import SharedModule from 'app/shared/shared.module';
import { Services } from 'app/admin/metrics/metrics.model';

@Component({
  standalone: true,
  selector: 'app-metrics-endpoints-requests',
  templateUrl: './metrics-endpoints-requests.component.html',
  imports: [SharedModule, TableModule],
})
export class MetricsEndpointsRequestsComponent {
  /**
   * object containing service related metrics
   */
  endpointsRequestsMetrics = input<Services>();

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  updating = input<boolean>();
}
