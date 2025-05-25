import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { IListOfEnumItem } from 'app/shared/common-types/ilist-of-enum-item';

@Injectable()
export class StandardRecordStatusService {
  public standardRecordStatusesSharedCollection: IListOfEnumItem[] = [];

  constructor(protected translateService: TranslateService) {
    this.defineEnums();

    this.translateService.onLangChange.subscribe(() => {
      this.defineEnums();
    });
  }

  public defineEnums(): IListOfEnumItem[] {
    this.standardRecordStatusesSharedCollection = [];

    this.standardRecordStatusesSharedCollection.push(
      {
        value: 'ACTIVE',
        label: this.translateService.instant('StandardRecordStatus.ACTIVE'),
      },
      {
        value: 'NOT_ACTIVE',
        label: this.translateService.instant('StandardRecordStatus.NOT_ACTIVE'),
      },
    );

    return this.standardRecordStatusesSharedCollection;
  }
}
