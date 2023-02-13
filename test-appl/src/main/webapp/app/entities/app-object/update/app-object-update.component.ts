import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { AppObjectFormService, AppObjectFormGroup } from './app-object-form.service';
import { IAppObject } from '../app-object.model';
import { AppObjectService } from '../service/app-object.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { AppObjectType } from 'app/entities/enumerations/app-object-type.model';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

@Component({
  selector: 'jhi-app-object-update',
  templateUrl: './app-object-update.component.html',
})
export class AppObjectUpdateComponent implements OnInit {
  isSaving = false;
  appObject: IAppObject | null = null;
  appObjectTypeValues = Object.keys(AppObjectType);
  standardRecordStatusValues = Object.keys(StandardRecordStatus);

  appObjectsSharedCollection: IAppObject[] = [];

  editForm: AppObjectFormGroup = this.appObjectFormService.createAppObjectFormGroup();

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected appObjectService: AppObjectService,
    protected appObjectFormService: AppObjectFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareAppObject = (o1: IAppObject | null, o2: IAppObject | null): boolean => this.appObjectService.compareAppObject(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appObject }) => {
      this.appObject = appObject;
      if (appObject) {
        this.updateForm(appObject);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('testapplApp.error', { message: err.message })),
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appObject = this.appObjectFormService.getAppObject(this.editForm);
    if (appObject.id !== null) {
      this.subscribeToSaveResponse(this.appObjectService.update(appObject));
    } else {
      this.subscribeToSaveResponse(this.appObjectService.create(appObject));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppObject>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(appObject: IAppObject): void {
    this.appObject = appObject;
    this.appObjectFormService.resetForm(this.editForm, appObject);

    this.appObjectsSharedCollection = this.appObjectService.addAppObjectToCollectionIfMissing<IAppObject>(
      this.appObjectsSharedCollection,
      appObject.parent
    );
  }

  protected loadRelationshipsOptions(): void {
    this.appObjectService
      .query()
      .pipe(map((res: HttpResponse<IAppObject[]>) => res.body ?? []))
      .pipe(
        map((appObjects: IAppObject[]) =>
          this.appObjectService.addAppObjectToCollectionIfMissing<IAppObject>(appObjects, this.appObject?.parent)
        )
      )
      .subscribe((appObjects: IAppObject[]) => (this.appObjectsSharedCollection = appObjects));
  }
}
