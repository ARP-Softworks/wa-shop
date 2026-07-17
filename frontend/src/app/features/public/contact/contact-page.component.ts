import { Component, inject } from '@angular/core';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';

@Component({
  selector: 'app-contact-page',
  standalone: true,
  templateUrl: './contact-page.component.html',
  styleUrl: './contact-page.component.scss',
})
export class ContactPageComponent {
  readonly contentApi = inject(PublicContentApiService);
  private readonly whatsapp = inject(WhatsappLinkService);

  get whatsappUrl(): string | null {
    return this.whatsapp.buildGeneralInquiryUrl();
  }
}
