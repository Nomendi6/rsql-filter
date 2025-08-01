import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { IListOfEnumItem } from 'app/shared/common-types/ilist-of-enum-item';

@Injectable()
export class AppObjectTypeService {
  public appObjectTypesSharedCollection: IListOfEnumItem[] = [];

  constructor(protected translateService: TranslateService) {
    this.defineEnums();

    this.translateService.onLangChange.subscribe(() => {
      this.defineEnums();
    });
  }

  public defineEnums(): IListOfEnumItem[] {
    this.appObjectTypesSharedCollection = [];

    this.appObjectTypesSharedCollection.push(
      {
        value: 'FUNCTIONAL_MODULE',
        label: this.translateService.instant('AppObjectType.FUNCTIONAL_MODULE'),
      },
      {
        value: 'FORM',
        label: this.translateService.instant('AppObjectType.FORM'),
      },
      {
        value: 'REPORT',
        label: this.translateService.instant('AppObjectType.REPORT'),
      },
      {
        value: 'ENTITY',
        label: this.translateService.instant('AppObjectType.ENTITY'),
      },
    );

    return this.appObjectTypesSharedCollection;
  }
}
