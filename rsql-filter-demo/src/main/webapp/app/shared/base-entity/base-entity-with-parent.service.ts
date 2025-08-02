import { Inject, Injectable, InjectionToken } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from 'app/core/request/request-util';
import { throwError } from 'rxjs/internal/observable/throwError';
import { ClassWithId } from './class-with-id';
import { RequestOptions } from './request-options';

export const SERVER_API_URL = new InjectionToken<string>('');

/**
 * A base service for performing CRUD operations on entities.
 * @template T The entity type extending ClassWithId.
 */
@Injectable()
export abstract class BaseEntityWithParentService<T extends ClassWithId> {
  protected readonly resourceUrl;
  protected readonly resourceSearchUrl;
  protected readonly resourceExportUrl;
  protected readonly resourceLovUrl;

  protected abstract http: HttpClient;

  protected constructor(
    @Inject(SERVER_API_URL) protected serverAPIUrl: string,
    @Inject(String) protected parentURLName: string,
    @Inject(String) protected entityURLName: string,
  ) {
    this.resourceUrl = this.serverAPIUrl + this.entityURLName;
    this.resourceSearchUrl = `${this.serverAPIUrl}_search/${this.entityURLName}`;
    this.resourceExportUrl = `${this.serverAPIUrl}${this.entityURLName}-list`;
    this.resourceLovUrl = `${this.serverAPIUrl}${this.entityURLName}/lov`;
  }

  /**
   * Creates a new entity.
   * @param parentId The parent ID.
   * @param entity The entity to create.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the created entity.
   */
  create(parentId: number | undefined | null, entity: T, entityName?: string): Observable<HttpResponse<T>> {
    parentId = parentId ?? -9876;
    const headers = this.createHeaders(entityName);
    return this.http.post<T>(this.getResourceUrl(parentId), entity, { headers, observe: 'response' });
  }

  /**
   * Updates an existing entity.
   * @param parentId The parent ID.
   * @param entity The entity to update.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the updated entity.
   */
  update(parentId: number | undefined | null, entity: T, entityName?: string): Observable<HttpResponse<T>> {
    if (entity.id == null) {
      return throwError(() => new Error('Entity ID is required for update operation.'));
    }
    parentId = parentId ?? -9876;
    const headers = this.createHeaders(entityName);
    return this.http.put<T>(`${this.getResourceUrl(parentId)}/${entity.id}`, entity, { headers, observe: 'response' });
  }

  /**
   * Partially updates an existing entity.
   * @param parentId The parent ID.
   * @param entity The entity with partial updates.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response containing the updated entity.
   */
  partialUpdate(parentId: number | undefined | null, entity: T, entityName?: string): Observable<HttpResponse<T>> {
    if (entity.id == null) {
      return throwError(() => new Error('Entity ID is required for update operation.'));
    }
    parentId = parentId ?? -9876;
    const headers = this.createHeaders(entityName);
    return this.http.patch<T>(`${this.getResourceUrl(parentId)}/${entity.id}`, entity, { headers, observe: 'response' });
  }

  /**
   * Retrieves an entity by its ID.
   * @param parentId The parent ID.
   * @param id The ID of the entity.
   * @returns An Observable of the HTTP response containing the entity.
   */
  find(parentId: number | undefined | null, id: number): Observable<HttpResponse<T>> {
    parentId = parentId ?? -9876;
    return this.http.get<T>(`${this.getResourceUrl(parentId)}/${id}`, { observe: 'response' });
  }

  /**
   * Queries entities with optional request parameters.
   * @param parentId The parent ID.
   * @param req The request parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  query(parentId: number | undefined | null, req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req, true);
    parentId = parentId ?? -9876;
    return this.http.get<T[]>(this.getResourceUrl(parentId), { params: options, observe: 'response' });
  }

  /**
   * Deletes an entity by its ID.
   * @param parentId The parent ID.
   * @param id The ID of the entity.
   * @param entityName Optional name of the entity.
   * @returns An Observable of the HTTP response.
   */
  delete(parentId: number | undefined | null, id: number, entityName?: string): Observable<HttpResponse<{}>> {
    parentId = parentId ?? -9876;
    const headers = this.createHeaders(entityName);
    return this.http.delete(`${this.getResourceUrl(parentId)}/${id}`, { headers, observe: 'response' });
  }

  /**
   * Searches for entities using Elastic or similar technology based on request parameters.
   * @param req The search parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  search(req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req);
    return this.http.get<T[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  /**
   * Exports data based on request parameters.
   * @param parentId The parent ID.
   * @param req The export parameters.
   * @returns An Observable of the HTTP response containing an array of entities.
   */
  exportData(parentId: number | undefined | null, req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req, true);
    parentId = parentId ?? -9876;
    return this.http.get<T[]>(this.getResourceExportUrl(parentId), { params: options, observe: 'response' });
  }

  /**
   * Retrieves a list of entities for a list of values.
   * @param parentId The parent ID.
   * @param req The request parameters.
   * @returns An Observable of the HTTP response containing an array
   */
  lov(parentId: number | undefined | null, req?: RequestOptions): Observable<HttpResponse<T[]>> {
    const options = createRequestOption(req);
    parentId = parentId ?? -9876;
    return this.http.get<T[]>(this.getResourceLovUrl(parentId), { params: options, observe: 'response' });
  }

  /**
   * Creates headers for an entity.
   * @param entityName The name of the entity.
   * @returns The headers.
   */
  private createHeaders(entityName?: string): HttpHeaders | undefined {
    return entityName ? new HttpHeaders({ 'X-ENTITY-NAME': entityName }) : undefined;
  }

  /**
   * Retrieves the URL for an entity.
   * @param parentId The parent ID.
   * @returns The URL.
   */
  protected getResourceUrl(parentId: number): string {
    return `${this.serverAPIUrl}${this.parentURLName}/${parentId}/${this.entityURLName}`;
  }

  /**
   * Retrieves the URL for exporting data.
   * @param parentId The parent ID.
   * @returns The URL.
   */
  protected getResourceExportUrl(parentId: number): string {
    return `${this.serverAPIUrl}${this.parentURLName}/${parentId}/${this.entityURLName}-list`;
  }

  /**
   * Retrieves the URL for a list of values.
   * @param parentId The parent ID.
   * @returns The URL.
   */
  protected getResourceLovUrl(parentId: number): string {
    return `${this.serverAPIUrl}${this.parentURLName}/${parentId}/${this.entityURLName}/lov`;
  }
}
