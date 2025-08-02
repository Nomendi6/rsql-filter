import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { MenuItem } from 'primeng/api';

@Injectable()
export class BreadcrumbService {
  private itemsSource = new Subject<MenuItem[]>();

  public setItems(items: MenuItem[]): void {
    this.itemsSource.next(items);
  }

  public getItems$(): Observable<MenuItem[]> {
    return this.itemsSource.asObservable();
  }
}
