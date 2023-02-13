import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { AppObjectFormService } from './app-object-form.service';
import { AppObjectService } from '../service/app-object.service';
import { IAppObject } from '../app-object.model';

import { AppObjectUpdateComponent } from './app-object-update.component';

describe('AppObject Management Update Component', () => {
  let comp: AppObjectUpdateComponent;
  let fixture: ComponentFixture<AppObjectUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let appObjectFormService: AppObjectFormService;
  let appObjectService: AppObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [AppObjectUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(AppObjectUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AppObjectUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    appObjectFormService = TestBed.inject(AppObjectFormService);
    appObjectService = TestBed.inject(AppObjectService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call AppObject query and add missing value', () => {
      const appObject: IAppObject = { id: 456 };
      const parent: IAppObject = { id: 75700 };
      appObject.parent = parent;

      const appObjectCollection: IAppObject[] = [{ id: 55950 }];
      jest.spyOn(appObjectService, 'query').mockReturnValue(of(new HttpResponse({ body: appObjectCollection })));
      const additionalAppObjects = [parent];
      const expectedCollection: IAppObject[] = [...additionalAppObjects, ...appObjectCollection];
      jest.spyOn(appObjectService, 'addAppObjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ appObject });
      comp.ngOnInit();

      expect(appObjectService.query).toHaveBeenCalled();
      expect(appObjectService.addAppObjectToCollectionIfMissing).toHaveBeenCalledWith(
        appObjectCollection,
        ...additionalAppObjects.map(expect.objectContaining)
      );
      expect(comp.appObjectsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const appObject: IAppObject = { id: 456 };
      const parent: IAppObject = { id: 49445 };
      appObject.parent = parent;

      activatedRoute.data = of({ appObject });
      comp.ngOnInit();

      expect(comp.appObjectsSharedCollection).toContain(parent);
      expect(comp.appObject).toEqual(appObject);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppObject>>();
      const appObject = { id: 123 };
      jest.spyOn(appObjectFormService, 'getAppObject').mockReturnValue(appObject);
      jest.spyOn(appObjectService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appObject });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appObject }));
      saveSubject.complete();

      // THEN
      expect(appObjectFormService.getAppObject).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(appObjectService.update).toHaveBeenCalledWith(expect.objectContaining(appObject));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppObject>>();
      const appObject = { id: 123 };
      jest.spyOn(appObjectFormService, 'getAppObject').mockReturnValue({ id: null });
      jest.spyOn(appObjectService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appObject: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appObject }));
      saveSubject.complete();

      // THEN
      expect(appObjectFormService.getAppObject).toHaveBeenCalled();
      expect(appObjectService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppObject>>();
      const appObject = { id: 123 };
      jest.spyOn(appObjectService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appObject });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(appObjectService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAppObject', () => {
      it('Should forward to appObjectService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(appObjectService, 'compareAppObject');
        comp.compareAppObject(entity, entity2);
        expect(appObjectService.compareAppObject).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
