import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { tap } from 'rxjs/operators';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { LANGUAGES } from 'app/config/language.constants';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { IUser, User } from '../user-management.model';
import { UserManagementStoreService } from './user-management-store.service';

@Injectable()
export class UserManagement$D$1FormGroupService {
  public editForm = new FormGroup({
    id: new FormControl<number | null>(null),
    login: new FormControl<string | null>(null, {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50),
        Validators.pattern('^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$'),
      ],
    }),
    firstName: new FormControl<string | null>(null, {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(50)],
    }),
    lastName: new FormControl<string | null>(null, {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(50)],
    }),
    email: new FormControl<string | null>(null, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
    }),
    activated: new FormControl<boolean>(true, { nonNullable: true }),
    langKey: new FormControl<string | null>('en', { nonNullable: true }),
    authorities: new FormControl<string[]>([], { nonNullable: true }),
  });
  public languages = LANGUAGES;
  public authorities: { name: string }[] = [];
  public hasCurrentRecord = false;
  private currentAccount: Account | null = null;
  private oldRecord?: IUser;
  private statusChangesSubscription?: Subscription;
  private isEdited = false;

  constructor(
    protected eventManager: EventManager,
    protected translateService: TranslateService,
    protected fb: FormBuilder,
    protected accountService: AccountService,
    public store: UserManagementStoreService,
  ) {
    this.eventManager.subscribe('UserRecordChange', event => {
      if (typeof event !== 'string') {
        if (event.content) {
          this.hasCurrentRecord = true;
          this.oldRecord = event.content as IUser;
          this.updateForm(event.content as IUser);
        } else {
          this.hasCurrentRecord = false;
        }
      }
    });
    this.eventManager.subscribe('UserSaveRecord', () => {
      this.save$().subscribe();
    });
    this.store.authorities().subscribe(authorities => {
      this.authorities = authorities;
    });
    this.accountService.identity().subscribe(account => {
      this.currentAccount = account;
    });
  }

  updateForm(newUser: IUser): void {
    this.editForm.reset();
    this.isEdited = false;
    this.subscribeToFormStatusChanges();
    this.editForm.patchValue({
      id: newUser.id,
      email: newUser.email,
      login: newUser.login,
      activated: newUser.activated ?? true,
      langKey: newUser.langKey ?? 'en',
      authorities: newUser.authorities,
      firstName: newUser.firstName,
      lastName: newUser.lastName,
    });

    // Disabled the 'activated' checkbox if the user is the current user
    // Or we're creating a new user
    if (newUser.id === undefined || this.currentAccount?.login === newUser.login) {
      this.editForm.get('activated')?.disable();
    } else {
      this.editForm.get('activated')?.enable();
    }
  }

  createFromForm(): IUser {
    return {
      ...new User(),
      id: this.editForm.get(['id'])!.value,
      email: this.editForm.get(['email'])!.value,
      login: this.editForm.get(['login'])!.value,
      activated: this.editForm.get(['activated'])!.value,
      langKey: this.editForm.get(['langKey'])!.value,
      authorities: this.editForm.get(['authorities'])!.value,
      firstName: this.editForm.get(['firstName'])!.value,
      lastName: this.editForm.get(['lastName'])!.value,
    };
  }

  save$(): Observable<HttpResponse<IUser>> {
    const newUser = this.createFromForm();
    if (newUser.id !== undefined) {
      return this.store.update(newUser).pipe(
        tap((res: HttpResponse<IUser>) => {
          this.eventManager.broadcast(new EventWithContent<IUser | undefined>('UserRecordUpdated', res.body ?? undefined));
        }),
      );
    }
    return this.store.create(newUser).pipe(
      tap((res: HttpResponse<IUser>) => {
        this.eventManager.broadcast(new EventWithContent<IUser | undefined>('UserRecordUpdated', res.body ?? undefined));
      }),
    );
  }

  cancelEdit(): void {
    if (this.oldRecord) {
      this.updateForm(this.oldRecord);
      if (this.oldRecord.id === undefined) {
        this.eventManager.broadcast('UserCancelAddNew');
      } else {
        this.eventManager.broadcast('UserCancelEdit');
      }
    }
  }

  subscribeToFormStatusChanges(): void {
    this.statusChangesSubscription = this.editForm.statusChanges.subscribe(() => {
      if (!this.editForm.pristine) {
        this.isEdited = true;
        this.unsubscribeFromFormStatusChanges();
        this.eventManager.broadcast('UserIsEdited');
      }
    });
  }

  unsubscribeFromFormStatusChanges(): void {
    if (this.statusChangesSubscription) {
      this.statusChangesSubscription.unsubscribe();
    }
  }
}
// Tpl: form/store/formgroup.service.ts | v.1.0.0
