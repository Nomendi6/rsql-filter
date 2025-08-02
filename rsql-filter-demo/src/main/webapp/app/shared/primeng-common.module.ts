import { AccordionModule } from 'primeng/accordion';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { ChartModule } from 'primeng/chart';
import { CheckboxModule } from 'primeng/checkbox';
import { ChipModule } from 'primeng/chip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { DialogService, DynamicDialogConfig, DynamicDialogModule, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SelectModule } from 'primeng/select';
import { FileUploadModule } from 'primeng/fileupload';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { MultiSelectModule } from 'primeng/multiselect';
import { NgModule } from '@angular/core';
import { OrderListModule } from 'primeng/orderlist';
import { PopoverModule } from 'primeng/popover';
import { PanelModule } from 'primeng/panel';
import { PasswordModule } from 'primeng/password';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SplitterModule } from 'primeng/splitter';
import { StepsModule } from 'primeng/steps';
import { TabsModule } from 'primeng/tabs';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { TreeTableModule } from 'primeng/treetable';
import { MenuModule } from 'primeng/menu';
import { CardModule } from 'primeng/card';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

@NgModule({
  imports: [
    AccordionModule,
    AutoCompleteModule,
    AvatarModule,
    ButtonModule,
    DatePickerModule,
    ChartModule,
    CheckboxModule,
    ConfirmDialogModule,
    DialogModule,
    SelectModule,
    DynamicDialogModule,
    FileUploadModule,
    ToggleSwitchModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    MessageModule,
    MultiSelectModule,
    OrderListModule,
    PopoverModule,
    PanelModule,
    PasswordModule,
    RadioButtonModule,
    SplitterModule,
    TabsModule,
    TableModule,
    ToastModule,
    TooltipModule,
    TreeTableModule,
    ChipModule,
    StepsModule,
    MenuModule,
    CardModule,
    IconFieldModule,
    InputIconModule,
  ],
  exports: [
    AccordionModule,
    AutoCompleteModule,
    AvatarModule,
    ButtonModule,
    DatePickerModule,
    ChartModule,
    CheckboxModule,
    ConfirmDialogModule,
    DialogModule,
    SelectModule,
    DynamicDialogModule,
    FileUploadModule,
    ToggleSwitchModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    MessageModule,
    MultiSelectModule,
    OrderListModule,
    PopoverModule,
    PanelModule,
    PasswordModule,
    RadioButtonModule,
    SplitterModule,
    TabsModule,
    TableModule,
    ToastModule,
    TooltipModule,
    TreeTableModule,
    ChipModule,
    StepsModule,
    MenuModule,
    CardModule,
    IconFieldModule,
    InputIconModule,
  ],
  providers: [ConfirmationService, DialogService, DynamicDialogRef, DynamicDialogConfig],
})
export class PrimeNGCommonModule {}
