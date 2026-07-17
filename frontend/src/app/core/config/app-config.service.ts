import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  readonly apiBaseUrl = environment.apiBaseUrl;
  readonly production = environment.production;
  readonly siteName = 'WA Shop';

  private readonly whatsappPhone = signal<string>('');

  whatsappPhoneValue = this.whatsappPhone.asReadonly();

  setWhatsappPhone(phone: string): void {
    this.whatsappPhone.set(phone);
  }
}
