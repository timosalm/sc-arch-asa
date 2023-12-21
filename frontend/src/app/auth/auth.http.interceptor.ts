import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {EMPTY, Observable, throwError} from "rxjs";
import {catchError} from 'rxjs/operators';
import {Router} from "@angular/router";
import {environment} from "../../environments/environment";

@Injectable()
export class AuthHttpInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError(error => {
        if (!environment.authConfig.enabled || (error instanceof HttpErrorResponse && error.status === 401)) {
          this.router.navigate(['/']);
          return EMPTY;
        }
        return throwError(error);
      }
    ));
  }
}
