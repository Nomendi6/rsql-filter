import { Injectable } from '@angular/core';
// import { IParty } from 'app/entities/customers/party/party.model';
// import { ICustomerAccount } from 'app/entities/customers/customer-account/customer-account.model';
// import { IBillingAccount } from 'app/entities/customers/billing-account/billing-account.model';
// import { IServiceAccount } from 'app/entities/customers/service-account/service-account.model';
// import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';

@Injectable({
  providedIn: 'root',
})
export class GlobalContextService {
  //  company: IParty | null | undefined;
  //  constructor(private eventManager: EventManager) {}
  //  setParty(company?: IParty): void {
  //    this.company = company;
  //    this.eventManager.broadcast(new EventWithContent<IParty | undefined>('GlobalContextPartyChange', this.company));
  //  }
  //  setPartyAndRemoveOthers(company?: IParty | null): void {
  //    this.company = company;
  //    if (this.organization?.rootParty?.id !== this.company?.id) {
  //      this.organization = undefined;
  //    }
  //    if (this.customerAccount?.company?.id !== this.company?.id) {
  //      this.customerAccount = undefined;
  //    }
  //    if (this.billingAccount?.company?.id !== this.company?.id) {
  //      this.billingAccount = undefined;
  //    }
  //    if (this.serviceAccount?.company?.id !== this.company?.id) {
  //      this.serviceAccount = undefined;
  //    }
  //    this.eventManager.broadcast(new EventWithContent<IParty | undefined>('GlobalContextPartyChange', this.company ?? undefined));
  //  }
  //  clearAll(): void {
  //    this.company = undefined;
  //    this.eventManager.broadcast(new EventWithContent<IParty | undefined>('GlobalContextPartyChange', this.company));
  //  }
}
