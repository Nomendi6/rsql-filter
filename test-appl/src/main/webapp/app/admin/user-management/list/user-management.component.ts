import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, combineLatest, take } from 'rxjs';
import { ConfirmationService, SortMeta } from 'primeng/api';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateService } from '@ngx-translate/core';

import SharedModule from 'app/shared/shared.module';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { BreadcrumbService } from 'app/layouts/main/breadcrumb.service';
import { andRsql, filterToRsql, getTableSort } from 'app/shared/util/request-util';
import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { FormHelpDialogService } from 'app/shared/form-help-dialog/form-help-dialog.service';
import { UserManagementStoreService } from '../store/user-management-store.service';
import { User } from '../user-management.model';
import UserManagement$AComponent from '../user-management-a/layout/user-management-a.component';

@Component({
  standalone: true,
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  imports: [SharedModule, UserManagement$AComponent, TableModule, ButtonModule, InputTextModule, TooltipModule],
})
export default class UserManagementComponent implements OnInit, OnDestroy {
  @ViewChild('newUserTable', { static: false })
  newUserTable!: Table;

  @Output() currentRecord = new EventEmitter<User>();

  @Input()
  set tag(tag: any) {
    this.parentTag = tag;
  }

  public parentTag?: any;
  public _parentRecord?: any;
  public currentRowIndex = 0;
  public _currentRecord?: User;
  public selectedRecord?: User;
  public tableFilters: any = {};
  public tableSort: string[] = [];
  public multiSortMeta: SortMeta[] = [];
  public isEditing = false;
  public initialFilter = '';
  public routeFilter = '';
  public globalSearchTerm = '';
  public selectedAll = false;
  public filterTypes: any = {
    id: 'number',
    email: 'string',
    createdAt: 'date',
    login: 'string',
    profiles: 'string',
    language: 'string',
    modifiedBy: 'string',
    modifiedDate: 'date',
  };

  users: User[] = [];
  eventSubscriptions: Subscription[] = [];
  isLoading = false;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  first = 0;
  loading = true;
  page?: number;
  addingNewRecord = false;
  showForm = true;

  constructor(
    protected store: UserManagementStoreService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected translateService: TranslateService,
    protected eventManager: EventManager,
    protected confirmationService: ConfirmationService,
    public formHelpDialogService: FormHelpDialogService,
    private breadcrumbService: BreadcrumbService,
  ) {}

  setBreadcrumb(): void {
    this.breadcrumbService.setItems([
      { label: this.translateService.instant('global.menu.home') },
      {
        label: this.translateService.instant('global.menu.entities.newUser'),
        routerLink: ['admin/user-management'],
      },
    ]);
  }

  setParentRecord(parentRecord: any): void {
    if (parentRecord) {
      this.showForm = true;
    } else {
      this.users = [];
      this.selectFirstRow();

      this.showForm = false;
      return;
    }

    if (this._parentRecord?.id) {
      if (parentRecord.id !== this._parentRecord.id) {
        this.currentRowIndex = 0;
      }
    }
    this._parentRecord = parentRecord;

    this.loadPage(1);
  }

  loadPage(page?: number, dontNavigate?: boolean, keepCurrentRecord?: boolean): void {
    this.isLoading = true;
    this.isEditing = false;
    this.addingNewRecord = false;
    const pageToLoad: number = page ?? this.page ?? 1;

    this.store
      .query({
        filter: this.getFilter(),
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe({
        next: (res: HttpResponse<User[]>) => {
          this.isLoading = false;
          this.onSuccess(res.body, res.headers, pageToLoad, !dontNavigate, keepCurrentRecord);
        },
        error: () => {
          this.isLoading = false;
        },
      });
  }

  ngOnInit(): void {
    this.setBreadcrumb();

    this.translateService.onLangChange.subscribe((/* params: LangChangeEvent */) => {
      this.setBreadcrumb();
    });

    this.routeFilter = this.activatedRoute.snapshot.queryParamMap.get('filter') ?? '';

    this.handleNavigation();

    this.eventSubscriptions.push(
      this.eventManager.subscribe('UserRecordUpdated', event => {
        if (typeof event !== 'string') {
          if (event.content) {
            Object.assign(this._currentRecord as User, event.content as User);
          }
          // after add new record remove the last record
          if (this.users.length > this.itemsPerPage) {
            this.users = this.users.slice(0, this.itemsPerPage);
          }
          if (this.addingNewRecord && this.users[0].id !== undefined) {
            this.addingNewRecord = false;
          }
          // broadcast RecordChange event
          this.onRowSelect();
        }
      }),
    );

    this.eventSubscriptions.push(
      this.eventManager.subscribe('UserCancelAddNew', () => {
        this.removeNewRecord();
      }),
    );

    this.eventSubscriptions.push(
      this.eventManager.subscribe('UserIsEdited', () => {
        this.isEditing = true;
      }),
    );

    this.eventSubscriptions.push(
      this.eventManager.subscribe('UserCancelEdit', () => {
        this.isEditing = false;
      }),
    );
  }

  ngOnDestroy(): void {
    this.eventSubscriptions.forEach(sub => {
      this.eventManager.destroy(sub);
    });
  }

  onLazyLoadEvent(event: TableLazyLoadEvent): void {
    // const queryParams = lazyLoadEventToRouterQueryParams(event, this.filtersDetails);
    // this.router.navigate(['/admin/user-management'], { queryParams });
    this.tableFilters = event.filters;
    this.itemsPerPage = event.rows ?? ITEMS_PER_PAGE;
    if (event.rows && event.first && event.rows > 0 && event.first > 0) {
      this.page = Math.floor(event.first / event.rows) + 1;
    } else {
      this.page = 1;
    }
    this.tableSort = getTableSort(event.multiSortMeta);
    this.loadPage();
  }

  trackId(index: number, item: User): number {
    return item.id!;
  }

  private getFilter(): string {
    const predefinedFilter = andRsql(this.initialFilter, this.routeFilter);
    let completeFilter = andRsql(this.getFilterForParentRecord(), predefinedFilter);
    const filter = filterToRsql(this.tableFilters, ['login', 'email'], this.filterTypes);
    completeFilter = andRsql(completeFilter, filter);
    return completeFilter;
  }

  private getFilterForParentRecord(): string {
    return '';
  }

  delete(newUser: User): void {
    this.confirmationService.confirm({
      key: 'UserManagementDeleteDialogHeading',
      header: this.translateService.instant('entity.delete.title'),
      rejectLabel: this.translateService.instant('entity.action.cancel'),
      acceptLabel: this.translateService.instant('entity.action.delete'),
      message: this.translateService.instant('userManagement.delete.question', { login: newUser.login }),
      rejectIcon: 'pi pi-ban',
      rejectButtonStyleClass: 'p-button-secondary',
      acceptIcon: 'pi pi-check',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        if (newUser.login) {
          this.store.delete(newUser.login).subscribe(() => {
            this.loadPage();
          });
        } else {
          this.removeNewRecord();
        }
      },
    });
  }

  protected sort(): string[] {
    let hasId = false;
    for (const s of this.tableSort) {
      if (s.startsWith('id')) {
        hasId = true;
      }
    }
    if (!hasId) {
      this.tableSort.push('id,asc');
    }
    return this.tableSort;
  }

  protected handleNavigation(): void {
    combineLatest([this.activatedRoute.data, this.activatedRoute.queryParamMap])
      .pipe(take(1))
      .subscribe(([data, params]) => {
        const page = params.get('page') ?? 0;
        const pageNumber = +page > 0 ? +page : 1;
        const sort: string = params.get('sort') ?? data.defaultSort;
        const splitSort = sort.split(',');
        const itemsPerPage = params.get('size');
        const itemsPerPageNumber = itemsPerPage !== null ? +itemsPerPage : ITEMS_PER_PAGE;

        for (let i = 0; i < splitSort.length; i += 2) {
          const field = splitSort[i];
          const order = splitSort[i + 1];
          this.tableSort.push(`${field},${order}`);
        }

        if (pageNumber !== this.page && pageNumber > 0 && itemsPerPageNumber > 0) {
          const calculatedSort = this.sort();
          this.multiSortMeta = calculatedSort.map(s => {
            const field = s.split(',')[0];
            const order = s.split(',')[1] === 'asc' ? 1 : -1;
            return { field, order };
          });

          this.first = (pageNumber - 1) * this.itemsPerPage;
          this.itemsPerPage = itemsPerPageNumber;

          this.loadPage(pageNumber, true);
        }
      });
  }

  protected onSuccess(data: User[] | null, headers: HttpHeaders, page: number, navigate: boolean, keepCurrentRecord?: boolean): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    const queryParams: { page: number; size: number; sort: string; filter?: string } = {
      page: this.page,
      size: this.itemsPerPage,
      sort: this.sort().join(','),
    };
    if (this.routeFilter.length > 0) {
      queryParams.filter = this.routeFilter;
    }

    this.users = data ?? [];
    this.selectFirstRow();
    if (navigate) {
      this.router.navigate(['/admin/user-management'], {
        queryParams,
      });
    }
  }

  /*
  selectRow(tableRow: User): void {
    if (this._currentRecord && this._currentRecord.id =tableRow.id) {
      return;
    }
    this._currentRecord = tableRow;
    this.onRowSelect();
  }
*/

  selectFirstRow(): void {
    if (this.users.length > 0) {
      this._currentRecord = this.users[0];
    } else {
      this._currentRecord = undefined;
    }
    this.selectedRecord = this._currentRecord;
    this.onRowSelect();
  }

  onRowSelect(): void {
    this.isEditing = false;
    this.currentRecord.emit(this._currentRecord);
    this.eventManager.broadcast(new EventWithContent<User | undefined>('UserRecordChange', this._currentRecord));
  }

  selectCurrentRecord(): void {
    let recordIsChanged = true;
    if (this.users.length > 0) {
      if (this.selectedRecord?.id) {
        this._currentRecord = this.users.find(item => item.id === this.selectedRecord!.id);
        if (JSON.stringify(this._currentRecord) === JSON.stringify(this.selectedRecord)) {
          recordIsChanged = false;
        }
      } else {
        this._currentRecord = this.users[0];
      }
    } else {
      this._currentRecord = undefined;
    }
    if (recordIsChanged) {
      this.onRowSelect();
    }
  }

  selectRow(): void {
    if (this.isEditing) {
      this.confirmationService.confirm({
        header: this.translateService.instant('entity.save.title'),
        message: this.translateService.instant('entity.save.message'),
        rejectLabel: this.translateService.instant('entity.action.cancel'),
        acceptLabel: this.translateService.instant('entity.action.delete'),
        rejectIcon: 'pi pi-ban',
        rejectButtonStyleClass: 'p-button-secondary',
        acceptIcon: 'pi pi-save',
        acceptButtonStyleClass: 'p-button-primary',
        accept: () => {
          this.selectedRecord = this._currentRecord;
          this.eventManager.broadcast('UserSaveRecord');
        },
        reject: () => {
          this._currentRecord = this.selectedRecord;
          this.onRowSelect();
        },
      });
    } else {
      this._currentRecord = this.selectedRecord;
      this.onRowSelect();
    }
  }

  createRecord(): void {
    // create a new record and make it a current record
    const newRecord: User = new User();

    this.users.unshift(newRecord);
    this.addingNewRecord = true;
    this.selectFirstRow();
  }

  removeNewRecord(): void {
    if (this.users.length > 0) {
      if (this.users[0].id === undefined) {
        this.users = this.users.slice(1);
        this.addingNewRecord = false;
        this.selectFirstRow();
      }
    }
  }

  globalSearch(): void {
    this.newUserTable.filterGlobal(this.globalSearchTerm, 'contains');
  }

  onSelectAllClick(): void {
    this.selectedAll = !this.selectedAll;
  }
}
