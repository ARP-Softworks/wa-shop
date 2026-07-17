import { DOCUMENT } from '@angular/common';
import { Injectable, Renderer2, RendererFactory2, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { catchError, of } from 'rxjs';
import { SeoApiService } from '../api/seo-api.service';
import { SeoPageMeta, mapSeoPageMeta } from '../../shared/models/seo.models';

const JSON_LD_ATTR = 'data-wa-jsonld';

const DEFAULT_META: SeoPageMeta = {
  title: 'WA Shop — iPhone nuevos y usados en Uruguay',
  description:
    'WA Shop: iPhone nuevos y usados, accesorios y servicio técnico en Uruguay. Consultá por WhatsApp.',
  canonicalUrl: null,
  robots: 'index,follow',
  ogTitle: 'WA Shop — iPhone nuevos y usados en Uruguay',
  ogDescription:
    'WA Shop: iPhone nuevos y usados, accesorios y servicio técnico en Uruguay. Consultá por WhatsApp.',
  ogImage: null,
  ogImageAlt: 'WA Shop',
  ogType: 'website',
  jsonLd: [],
};

@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly document = inject(DOCUMENT);
  private readonly seoApi = inject(SeoApiService);
  private readonly renderer: Renderer2;

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);
  }

  applyFromPath(path: string): void {
    const cleanPath = this.stripQuery(path);
    this.seoApi
      .getPageMeta(cleanPath)
      .pipe(catchError(() => of(null)))
      .subscribe((response) => {
        if (!response) {
          this.apply({
            ...DEFAULT_META,
            title: `${DEFAULT_META.title}`,
            robots: cleanPath === '/404' ? 'noindex,follow' : DEFAULT_META.robots,
          });
          return;
        }
        this.apply(mapSeoPageMeta(response));
      });
  }

  apply(meta: SeoPageMeta): void {
    this.title.setTitle(meta.title || DEFAULT_META.title);

    this.setNamedMeta('description', meta.description || DEFAULT_META.description);
    this.setNamedMeta('robots', meta.robots || DEFAULT_META.robots);

    this.setPropertyMeta('og:title', meta.ogTitle || meta.title || DEFAULT_META.title);
    this.setPropertyMeta(
      'og:description',
      meta.ogDescription || meta.description || DEFAULT_META.description
    );
    this.setPropertyMeta('og:type', meta.ogType || 'website');
    this.setPropertyMeta('og:locale', 'es_UY');
    this.setPropertyMeta('og:site_name', 'WA Shop');
    if (meta.ogImage) {
      this.setPropertyMeta('og:image', meta.ogImage);
    } else {
      this.removePropertyMeta('og:image');
    }
    if (meta.ogImageAlt) {
      this.setPropertyMeta('og:image:alt', meta.ogImageAlt);
    } else {
      this.removePropertyMeta('og:image:alt');
    }
    if (meta.canonicalUrl) {
      this.setPropertyMeta('og:url', meta.canonicalUrl);
    } else {
      this.removePropertyMeta('og:url');
    }

    this.setNamedMeta('twitter:card', 'summary_large_image');
    this.setNamedMeta('twitter:title', meta.ogTitle || meta.title || DEFAULT_META.title);
    this.setNamedMeta(
      'twitter:description',
      meta.ogDescription || meta.description || DEFAULT_META.description
    );
    if (meta.ogImage) {
      this.setNamedMeta('twitter:image', meta.ogImage);
    } else {
      this.removeNamedMeta('twitter:image');
    }

    this.setCanonical(meta.canonicalUrl);
    this.replaceJsonLd(meta.jsonLd ?? []);
  }

  applyNotFound(): void {
    this.apply({
      ...DEFAULT_META,
      title: 'Página no encontrada | WA Shop',
      description: 'La página solicitada no existe o ya no está publicada.',
      robots: 'noindex,follow',
      ogTitle: 'Página no encontrada | WA Shop',
      jsonLd: [],
      statusCode: 404,
    });
  }

  private stripQuery(path: string): string {
    const withoutQuery = path.split('?')[0] || '/';
    if (!withoutQuery.startsWith('/')) {
      return `/${withoutQuery}`;
    }
    return withoutQuery || '/';
  }

  private setNamedMeta(name: string, content: string): void {
    if (this.meta.getTag(`name="${name}"`)) {
      this.meta.updateTag({ name, content });
    } else {
      this.meta.addTag({ name, content });
    }
  }

  private removeNamedMeta(name: string): void {
    this.meta.removeTag(`name="${name}"`);
  }

  private setPropertyMeta(property: string, content: string): void {
    if (this.meta.getTag(`property="${property}"`)) {
      this.meta.updateTag({ property, content });
    } else {
      this.meta.addTag({ property, content });
    }
  }

  private removePropertyMeta(property: string): void {
    this.meta.removeTag(`property="${property}"`);
  }

  private setCanonical(url: string | null | undefined): void {
    const head = this.document.head;
    if (!head) {
      return;
    }
    let link = head.querySelector('link[rel="canonical"]') as HTMLLinkElement | null;
    if (!url) {
      if (link) {
        this.renderer.removeChild(head, link);
      }
      return;
    }
    if (!link) {
      link = this.renderer.createElement('link') as HTMLLinkElement;
      this.renderer.setAttribute(link, 'rel', 'canonical');
      this.renderer.appendChild(head, link);
    }
    this.renderer.setAttribute(link, 'href', url);
  }

  private replaceJsonLd(scripts: string[]): void {
    const head = this.document.head;
    if (!head) {
      return;
    }
    head.querySelectorAll(`script[${JSON_LD_ATTR}]`).forEach((node) => {
      this.renderer.removeChild(head, node);
    });

    for (const raw of scripts) {
      if (!raw?.trim()) {
        continue;
      }
      const script = this.renderer.createElement('script') as HTMLScriptElement;
      this.renderer.setAttribute(script, 'type', 'application/ld+json');
      this.renderer.setAttribute(script, JSON_LD_ATTR, 'true');
      script.text = raw;
      this.renderer.appendChild(head, script);
    }
  }
}
