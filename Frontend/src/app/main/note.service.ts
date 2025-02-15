import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {environment} from "../../environments/environments";



@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private readonly _baseUrl: string;

  private usersInNote = new BehaviorSubject<number>(0);
  private NoteDetail = new BehaviorSubject<number>(0);
  constructor(private http: HttpClient) {
    this._baseUrl = environment.apiUrl;
  }

  get(id: number): void {

    this.http.get<any>(`${this._baseUrl}notes/${id}`).subscribe(
      response => {
        console.log('ug');
        if (response) {

          this.usersInNote.next(response.userNotes.length);
          this.NoteDetail.next(response)
        }
      },
      error => {
        console.error('Error Occurred:', error);
      }
    );
  }

  getUsersInNOte(): Observable<number> {
    return this.usersInNote.asObservable();
  }
  getNotesDetail(): Observable<any> {
    return this.NoteDetail.asObservable();
  }

  createNote(form: any): Observable<any>{
    let queryParams = new HttpParams();
    queryParams = queryParams.append('title', form.title);
    queryParams = queryParams.append('content', form.content)
    queryParams = queryParams.append('userId', form.userId)
    return this.http.post<any>(`${this._baseUrl}notes`, '', {params: queryParams});
  }

  shareNote(noteId: number) {
    let queryParams = new HttpParams();
    queryParams = queryParams.append('accessLevel', 'EDITOR')
    return this.http.post<any>(`${this._baseUrl}share/create/${noteId}`, '', {params: queryParams});
  }
  assignUser(noteId: number, userId: number){
    return this.http.post<any>(`${this._baseUrl}notes/${noteId}/assignUser?userId=${userId}` , '');
  }
}
