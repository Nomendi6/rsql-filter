import { Inject, Injectable, InjectionToken } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from 'app/core/request/request-util';
import { throwError } from 'rxjs/internal/observable/throwError';
import { ClassWithId } from './class-with-id';
import { RequestOptions } from './request-options';

// Define injection tokens
export const SERVER_API_URL = new InjectionToken<string>('');
export const HEADER_ENTITY_NAME = 'X-ENTITY-NAME';

/**
 * A base service for performing CRUD operations on entities.
 * @template T The entity type extending ClassWithId.
 */
@Injectable()
export abstract class BaseEntityService<T extends ClassWithId> {
  protected readonly resourceUrl: string;
  protected readonly resourceSearchUrl: string;
  protected readonly resourceExportUrl: string;
  protected readonly resourceLovUrl: string;

  protected abstract http: HttpClient;

  protected constructor(
    @Inject(SERVER_API_URL) protected serverAPIUrl: string,
    @Inject(String) protected entityURLName: string,
  ) {
    this.resourceUrl = `${this.serverAPIUrl}${this.entityURLName}`;
    this.resourceSearchUrl = `${this.serverAPIUrl}_search/${this.entityURLName}`;
    this.resourceExportUrl = `${this.serverAPIUrl}${this.entityURLName}-list`;
    this.resourceLovUrl = `${this.serverAPIUrl}${this.entityURLName}/lov`;
  }

  /**
   * Creates a new entity.
   * @param entity The entity to create.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the created entity.
   */
  create(entity: T, entityName?: string): Observable<HttpResponse<T>> {
    const headers = this.createHeaders(entityName);
    return this.http.post<T>(this.resourceUrl, entity, { headers, observe: 'response' });
  }

  /**
   * Updates an existing entity.
   * @param entity The entity to update.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the updated entity.
   */
  update(entity: T, entityName?: string): Observable<HttpResponse<T>> {
    if (entity.id == null) {
      return throwError(() => new Error('Entity ID is required for update operation.'));
    }
    const headers = this.createHeaders(entityName);
    return this.http.put<T>(`${this.resourceUrl}/${entity.id}`, entity, { headers, observe: 'response' });
  }

  /**
   * Partially updates an existing entity.
   * @param entity The entity with partial updates.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the updated entity.
   */
  partialUpdate(entity: Partial<T> & ClassWithId, entityName?: string): Observable<HttpResponse<T>> {
    if (entity.id == null) {
      return throwError(() => new Error('Entity ID is required for partial update operation.'));
    }
    const headers = this.createHeaders(entityName);
    return this.http.patch<T>(`${this.resourceUrl}/${entity.id}`, entity, { headers, observe: 'response' });
  }

  /**
   * Retrieves an entity by its ID.
   * @param id The ID of the entity.
   * @returns An Observable of the HTTP response containing the entity.
   */
  find(id: number): Observable<HttpResponse<T>> {
    return this.http.get<T>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  /**
   * Queries entities with optional request parameters.
   * @param req The request parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  query(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req, true);
    return this.http.get<T[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  /**
   * Queries all entities with optional request parameters.
   * @param req The request parameters.
   * @returns An Observable of the HTTP response containing an array of all entities.
   */
  queryAll(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req, true);
    return this.http.get<T[]>(`${this.resourceUrl}/all`, { params: options, observe: 'response' });
  }

  /**
   * Deletes an entity by its ID.
   * @param id The ID of the entity to delete.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response.
   */
  delete(id: number, entityName?: string): Observable<HttpResponse<{}>> {
    const headers = this.createHeaders(entityName);
    return this.http.delete(`${this.resourceUrl}/${id}`, { headers, observe: 'response' });
  }

  /**
   * Searches for entities based on request parameters.
   * @param req The search parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  search(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req);
    return this.http.get<T[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  /**
   * Exports data based on request parameters.
   * @param req The export parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  exportData(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req, true);
    return this.http.get<T[]>(this.resourceExportUrl, { params: options, observe: 'response' });
  }

  /**
   * Retrieves a list of values (LOV) based on request parameters.
   * @param req The request parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  lov(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req);
    return this.http.get<T[]>(this.resourceLovUrl, { params: options, observe: 'response' });
  }

  /**
   * Creates HTTP headers with an optional entity name.
   * @param entityName The name of the entity.
   * @returns The HttpHeaders object or undefined.
   */
  private createHeaders(entityName?: string): HttpHeaders | undefined {
    return entityName ? new HttpHeaders({ [HEADER_ENTITY_NAME]: entityName }) : undefined;
  }
}
