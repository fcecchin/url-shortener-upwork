import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ShortenedUrl} from "../shortened-url.model";

@Injectable({
  providedIn: 'root'
})
export class ShortenerService {

  constructor(private http: HttpClient) { }

  createShortenedUrl(url: string, validDays: number = 1) {
    return this.http.post<ShortenedUrl>('http://localhost:8080/', {"url": url, "valid-days": validDays});
  }
}
