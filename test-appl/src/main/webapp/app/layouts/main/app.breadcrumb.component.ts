import { Component, OnDestroy, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { MenuItem } from 'primeng/api';
import { BreadcrumbService } from './breadcrumb.service';

@Component({
  standalone: true,
  selector: 'app-breadcrumb',
  templateUrl: './app.breadcrumb.component.html',
})
export class AppBreadcrumbComponent implements OnDestroy {
  public items: MenuItem[] = [];
  private subscription: Subscription;
  private breadcrumbService = inject(BreadcrumbService);

  constructor() {
    this.subscription = this.breadcrumbService.getItems$().subscribe(response => {
      this.items = response;
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
