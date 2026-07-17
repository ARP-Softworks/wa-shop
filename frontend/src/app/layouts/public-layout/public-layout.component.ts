import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { PublicContentApiService } from '../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../core/analytics/analytics.service';
import { CartService } from '../../core/cart/cart.service';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './public-layout.component.html',
  styleUrl: './public-layout.component.scss',
})
export class PublicLayoutComponent implements OnInit {
  readonly contentApi = inject(PublicContentApiService);
  readonly cart = inject(CartService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);

  menuOpen = false;
  readonly currentYear = new Date().getFullYear();

  ngOnInit(): void {
    this.contentApi.loadSettings().subscribe();
  }

  get whatsappUrl(): string | null {
    return this.whatsapp.buildGeneralInquiryUrl();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  onWhatsappClick(): void {
    this.analytics.trackWhatsappClick('nav');
  }
}
