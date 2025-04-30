import { Component, OnInit, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';

import SharedModule from 'app/shared/shared.module';
import { GatewayRoutesService } from './gateway-routes.service';
import { GatewayRoute } from './gateway-route.model';

@Component({
  standalone: true,
  selector: 'app-gateway',
  templateUrl: './gateway.component.html',
  providers: [GatewayRoutesService],
  imports: [SharedModule, ButtonModule],
})
export default class GatewayComponent implements OnInit {
  gatewayRoutes: GatewayRoute[] = [];
  updatingRoutes = false;

  private gatewayRoutesService = inject(GatewayRoutesService);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.updatingRoutes = true;
    this.gatewayRoutesService.findAll().subscribe(gatewayRoutes => {
      this.gatewayRoutes = gatewayRoutes;
      this.updatingRoutes = false;
    });
  }
}
