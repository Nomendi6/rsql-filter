import { expect } from '@jest/globals';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormHelpDialogComponent } from './form-help-dialog.component';
import { FormHelpDialogService } from './form-help-dialog.service';

describe('FormHelpDialogComponent', () => {
  let component: FormHelpDialogComponent;
  let fixture: ComponentFixture<FormHelpDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormHelpDialogComponent],
      providers: [FormHelpDialogService],
    })
      .overrideTemplate(FormHelpDialogComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FormHelpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
