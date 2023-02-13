import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAppObject } from '../app-object.model';
import { DataUtils } from 'app/core/util/data-util.service';

@Component({
  selector: 'jhi-app-object-detail',
  templateUrl: './app-object-detail.component.html',
})
export class AppObjectDetailComponent implements OnInit {
  appObject: IAppObject | null = null;

  constructor(protected dataUtils: DataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appObject }) => {
      this.appObject = appObject;
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  previousState(): void {
    window.history.back();
  }
}
