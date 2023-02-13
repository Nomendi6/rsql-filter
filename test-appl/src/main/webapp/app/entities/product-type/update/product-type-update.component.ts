import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { ProductTypeFormService, ProductTypeFormGroup } from './product-type-form.service';
import { IProductType } from '../product-type.model';
import { ProductTypeService } from '../service/product-type.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { StandardRecordStatus } from 'app/entities/enumerations/standard-record-status.model';

@Component({
  selector: 'jhi-product-type-update',
  templateUrl: './product-type-update.component.html',
})
export class ProductTypeUpdateComponent implements OnInit {
  isSaving = false;
  productType: IProductType | null = null;
  standardRecordStatusValues = Object.keys(StandardRecordStatus);

  editForm: ProductTypeFormGroup = this.productTypeFormService.createProductTypeFormGroup();

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected productTypeService: ProductTypeService,
    protected productTypeFormService: ProductTypeFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ productType }) => {
      this.productType = productType;
      if (productType) {
        this.updateForm(productType);
      }
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
    const productType = this.productTypeFormService.getProductType(this.editForm);
    if (productType.id !== null) {
      this.subscribeToSaveResponse(this.productTypeService.update(productType));
    } else {
      this.subscribeToSaveResponse(this.productTypeService.create(productType));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IProductType>>): void {
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

  protected updateForm(productType: IProductType): void {
    this.productType = productType;
    this.productTypeFormService.resetForm(this.editForm, productType);
  }
}
