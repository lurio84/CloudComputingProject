import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";



@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private readonly _baseUrl: string;

  private usersInNote = new BehaviorSubject<number>(0);
  private NoteDetail = new BehaviorSubject<number>(0);
  constructor(private http: HttpClient) {
    this._baseUrl = 'http://localhost:8080/';
  }

  get(id: number): void {
    console.log('Requesting data...');

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

}
