import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Table, TableModule } from 'primeng/table';
import { AccordionModule } from 'primeng/accordion';
import { InputTextModule } from 'primeng/inputtext';

import SharedModule from 'app/shared/shared.module';
import { SortService, sortStateSignal } from 'app/shared/sort';
import { ConfigurationService } from './configuration.service';
import { Bean, PropertySource } from './configuration.model';

@Component({
  standalone: true,
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  imports: [SharedModule, FormsModule, TableModule, InputTextModule, AccordionModule],
})
export default class ConfigurationComponent implements OnInit {
  @ViewChild('propertiesTable', { static: false })
  propertiesTable!: Table;

  allBeans = signal<Bean[] | undefined>(undefined);
  beansFilter = signal<string>('');
  propertySources = signal<PropertySource[]>([]);
  sortState = sortStateSignal({ predicate: 'prefix', order: 'asc' });
  beans = computed(() => {
    let data = this.allBeans() ?? [];
    const beansFilter = this.beansFilter();
    if (beansFilter) {
      data = data.filter(bean => bean.prefix.toLowerCase().includes(beansFilter.toLowerCase()));
    }

    const { order, predicate } = this.sortState();
    if (predicate && order) {
      data = data.sort(this.sortService.startSort({ predicate, order }));
    }
    return data;
  });

  private sortService = inject(SortService);
  private configurationService = inject(ConfigurationService);

  ngOnInit(): void {
    this.configurationService.getBeans().subscribe(beans => {
      this.allBeans.set(beans);
    });

    this.configurationService.getPropertySources().subscribe(propertySources => this.propertySources.set(propertySources));
  }

  public clear(table: Table): void {
    table.clear();
  }

  public globalSearch(): void {
    this.propertiesTable.filterGlobal(this.beansFilter(), 'contains');
  }

  public getProperties(propertySource: PropertySource): any[] {
    // Extract list of keys/values from properties
    return Object.keys(propertySource.properties).map(key => ({
      key,
      value: propertySource.properties[key].value,
      origin: propertySource.properties[key].origin,
    }));
  }
}
