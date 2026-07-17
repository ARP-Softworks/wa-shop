import { TestBed } from '@angular/core/testing';
import { Meta, Title } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { SeoService } from './seo.service';
import { SeoPageMeta } from '../../shared/models/seo.models';

describe('SeoService', () => {
  let service: SeoService;
  let title: Title;
  let meta: Meta;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), SeoService],
    });
    service = TestBed.inject(SeoService);
    title = TestBed.inject(Title);
    meta = TestBed.inject(Meta);
    document.head.querySelectorAll('script[data-wa-jsonld]').forEach((n) => n.remove());
    document.head.querySelectorAll('link[rel="canonical"]').forEach((n) => n.remove());
  });

  it('applies title, description and canonical without duplicate json-ld', () => {
    const pageMeta: SeoPageMeta = {
      title: 'iPhone en Uruguay | WA Shop',
      description: 'Catálogo de iPhone nuevos y usados.',
      canonicalUrl: 'https://washop.uy/iphone',
      robots: 'index,follow',
      ogTitle: 'iPhone en Uruguay | WA Shop',
      ogDescription: 'Catálogo de iPhone nuevos y usados.',
      ogImage: 'https://cdn.example/og.jpg',
      ogImageAlt: 'iPhone',
      ogType: 'website',
      jsonLd: ['{"@type":"Organization","name":"WA Shop"}'],
    };

    service.apply(pageMeta);

    expect(title.getTitle()).toBe('iPhone en Uruguay | WA Shop');
    expect(meta.getTag('name="description"')?.content).toBe('Catálogo de iPhone nuevos y usados.');
    expect(meta.getTag('name="robots"')?.content).toBe('index,follow');
    expect(meta.getTag('property="og:title"')?.content).toBe('iPhone en Uruguay | WA Shop');
    expect(meta.getTag('name="twitter:card"')?.content).toBe('summary_large_image');

    const canonical = document.head.querySelector('link[rel="canonical"]') as HTMLLinkElement;
    expect(canonical?.href).toContain('https://washop.uy/iphone');

    expect(document.head.querySelectorAll('script[data-wa-jsonld]').length).toBe(1);

    service.apply({
      ...pageMeta,
      jsonLd: ['{"@type":"WebSite","name":"WA Shop"}', '{"@type":"Store","name":"WA Shop"}'],
    });

    const scripts = document.head.querySelectorAll('script[data-wa-jsonld]');
    expect(scripts.length).toBe(2);
    expect(scripts[0].textContent).toContain('WebSite');
    expect(scripts[1].textContent).toContain('Store');
  });

  it('clears previous json-ld when applying empty list', () => {
    service.apply({
      title: 'T1',
      description: 'D1',
      canonicalUrl: null,
      robots: 'noindex,follow',
      jsonLd: ['{"a":1}'],
    });
    expect(document.head.querySelectorAll('script[data-wa-jsonld]').length).toBe(1);

    service.apply({
      title: 'T2',
      description: 'D2',
      canonicalUrl: null,
      robots: 'noindex,follow',
      jsonLd: [],
    });
    expect(document.head.querySelectorAll('script[data-wa-jsonld]').length).toBe(0);
  });
});
