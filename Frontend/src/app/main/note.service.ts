import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private readonly _baseUrl: string;

  constructor(private http: HttpClient) {
    this._baseUrl = 'http://localhost:8080/';
  }

  get(id: number): Observable<any> {
    return this.http
      .get<any>(`${this._baseUrl}notes/${id}`)
  }
}
