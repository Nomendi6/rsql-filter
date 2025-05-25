import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { expect } from '@jest/globals';
import { NameFormattingService } from './name-formatting.service';

describe('NameFormattingService', () => {
  let service: NameFormattingService;
  // add FormBuilder to inject it into the service
  let formBuilder: FormBuilder;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FormBuilder],
    });
    service = TestBed.inject(NameFormattingService);
    formBuilder = TestBed.inject(FormBuilder);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should concat two names', () => {
    const editForm = formBuilder.group({
      id: [],
      person1firstName: [''],
      person1lastName: [''],
      person1name: [''],
    });
    editForm.patchValue({ person1firstName: 'John', person1lastName: 'Doe' });
    service.formatName(editForm, 'person1');
    const formattedName = editForm.controls.person1name.value;
    expect(formattedName).toEqual('John Doe');
  });

  it('should concat first name', () => {
    const editForm = formBuilder.group({
      id: [],
      person1firstName: [''],
      person1lastName: [''],
      person1name: [''],
    });
    editForm.patchValue({ person1firstName: 'John' });
    service.formatName(editForm, 'person1');
    const formattedName = editForm.controls.person1name.value;
    expect(formattedName).toEqual('John');
  });

  it('should concat last name', () => {
    const editForm = formBuilder.group({
      id: [],
      person1firstName: [''],
      person1lastName: [''],
      person1name: [''],
    });
    editForm.patchValue({ person1lastName: 'Doe' });
    service.formatName(editForm, 'person1');
    const formattedName = editForm.controls.person1name.value;
    expect(formattedName).toEqual('Doe');
  });
});
