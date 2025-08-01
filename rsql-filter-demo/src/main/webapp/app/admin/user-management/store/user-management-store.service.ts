import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Pagination } from 'app/core/request/request.model';
import dayjs from 'dayjs/esm';
import { IUser } from '../user-management.model';

export type EntityResponseType = HttpResponse<IUser>;
export type EntityArrayResponseType = HttpResponse<IUser[]>;

@Injectable({ providedIn: 'root' })
export class UserManagementStoreService {
  private resourceUrl = this.applicationConfigService.getEndpointFor('api/admin/users');

  constructor(
    private http: HttpClient,
    private applicationConfigService: ApplicationConfigService,
  ) {}

  create(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http
      .post<IUser>(this.resourceUrl, user, { observe: 'response' })
      .pipe(map((res: HttpResponse<IUser>) => this.convertDateFromServer(res)));
  }

  update(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http
      .put<IUser>(this.resourceUrl, user, { observe: 'response' })
      .pipe(map((res: HttpResponse<IUser>) => this.convertDateFromServer(res)));
  }

  find(login: string): Observable<HttpResponse<IUser>> {
    return this.http
      .get<IUser>(`${this.resourceUrl}/${login}`, { observe: 'response' })
      .pipe(map((res: HttpResponse<IUser>) => this.convertDateFromServer(res)));
  }

  query(req?: Pagination): Observable<HttpResponse<IUser[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<IUser[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: HttpResponse<IUser[]>) => this.convertDateArrayFromServer(res)));
  }

  delete(login: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${login}`, { observe: 'response' });
  }

  authorities(): Observable<{ name: string }[]> {
    return this.http.get<{ name: string }[]>(this.applicationConfigService.getEndpointFor('api/authorities'));
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.createdDate = res.body.createdDate ? dayjs(res.body.createdDate) : undefined;
      res.body.lastModifiedDate = res.body.lastModifiedDate ? dayjs(res.body.lastModifiedDate) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((user: IUser) => {
        user.createdDate = user.createdDate ? dayjs(user.createdDate) : undefined;
        user.lastModifiedDate = user.lastModifiedDate ? dayjs(user.lastModifiedDate) : undefined;
      });
    }
    return res;
  }
}
