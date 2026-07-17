import { TestBed } from '@angular/core/testing';
import { AppConfigService } from './app-config.service';

describe('AppConfigService', () => {
  it('exposes api base url', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(AppConfigService);
    expect(service.apiBaseUrl).toBe('/api');
    expect(service.siteName).toBe('WA Shop');
  });
});
