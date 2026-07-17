import { WhatsappLinkService } from './whatsapp-link.service';
import { PublicContentApiService } from '../api/public-content-api.service';

describe('WhatsappLinkService', () => {
  it('builds encoded message omitting empty fields', () => {
    const contentApi = {
      settings: () => ({ whatsappNumber: '+598 91 234 567' }),
    } as unknown as PublicContentApiService;

    const service = new WhatsappLinkService(contentApi);
    const url = service.buildProductInquiryUrl({
      name: 'iPhone 14',
      condition: 'USED',
      storageCapacity: '128GB',
      color: 'Midnight',
      batteryHealth: 92,
      currency: 'UYU',
      price: 28990,
    });

    expect(url).toContain('https://wa.me/59891234567?text=');
    const text = decodeURIComponent(url!.split('text=')[1]);
    expect(text).toContain('iPhone 14');
    expect(text).toContain('usado');
    expect(text).toContain('128GB');
    expect(text).toContain('color Midnight');
    expect(text).toContain('batería 92%');
    expect(text).toContain('UYU');
    expect(text).toContain('¿Sigue disponible?');
  });

  it('omits color and battery when empty', () => {
    const contentApi = {
      settings: () => ({ whatsappNumber: '59890000000' }),
    } as unknown as PublicContentApiService;

    const service = new WhatsappLinkService(contentApi);
    const url = service.buildProductInquiryUrl({
      name: 'iPhone 15',
      condition: 'NEW',
      storageCapacity: null,
      color: null,
      batteryHealth: null,
      currency: 'UYU',
      price: 42990,
    });

    const text = decodeURIComponent(url!.split('text=')[1]);
    expect(text).not.toContain('color');
    expect(text).not.toContain('batería');
    expect(text).toContain('nuevo');
  });
});
