import { NgModule } from '@angular/core';
import { PrimeNGCommonModule } from 'app/shared/primeng-common.module';
import { SharedLibsModule } from './shared-libs.module';
import { FindLanguageFromKeyPipe } from './language';
import TranslateDirective from './language/translate.directive';
import HasAnyAuthorityDirective from './auth/has-any-authority.directive';
import DurationPipe from './date/duration.pipe';
import FormatMediumDatetimePipe from './date/format-medium-datetime.pipe';
import FormatMediumDatePipe from './date/format-medium-date.pipe';
import { SortByDirective, SortDirective } from './sort';
import ItemCountComponent from './pagination/item-count.component';

@NgModule({
  imports: [
    SharedLibsModule,
    PrimeNGCommonModule,
    FindLanguageFromKeyPipe,
    TranslateDirective,
    HasAnyAuthorityDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    SortByDirective,
    SortDirective,
    ItemCountComponent,
  ],
  exports: [
    SharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateDirective,
    HasAnyAuthorityDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    SortByDirective,
    SortDirective,
    ItemCountComponent,
    PrimeNGCommonModule,
  ],
})
export default class SharedModule {}
