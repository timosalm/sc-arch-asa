import { Component } from '@angular/core';
import { authCodeFlowConfig } from './auth/auth.config';
import {OAuthService} from "angular-oauth2-oidc";
import { Clipboard } from '@angular/cdk/clipboard';
import {filter} from "rxjs";
import {environment} from "../environments/environment";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  constructor(private oauthService: OAuthService, private clipboard: Clipboard) {
    if (environment.authConfig.enabled) {
      this.oauthService.configure(authCodeFlowConfig);
      this.oauthService.loadDiscoveryDocumentAndTryLogin();
      this.oauthService.events
        .pipe(filter((e) => e.type === 'token_received'))
        .subscribe((_) => this.oauthService.loadUserProfile());
    }
  }

  get userName(): string {
    const claims = this.oauthService.getIdentityClaims();
    if (!claims) return "";
    return claims['preferred_username' as keyof typeof claims];
  }

  get isLoggedIn(): boolean {
    return environment.authConfig.enabled && this.oauthService.hasValidIdToken();
  }

  logout() {
    this.oauthService.revokeTokenAndLogout();
    location.reload();
  }

  copyOicToken() {
    const idToken = this.oauthService.getIdToken();
    this.clipboard.copy(idToken);
  }
}
