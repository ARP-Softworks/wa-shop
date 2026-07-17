/** Normalized page meta used by SeoService (DOM updates). */
export interface SeoPageMeta {
  title: string;
  description: string;
  canonicalUrl: string | null;
  robots: string;
  ogTitle?: string | null;
  ogDescription?: string | null;
  ogImage?: string | null;
  ogImageAlt?: string | null;
  ogType?: string | null;
  jsonLd: string[];
  statusCode?: number | null;
}

/**
 * Raw response from GET /api/public/seo/page.
 * Matches backend SeoPageMetaResponse (Jackson field names).
 */
export interface SeoPageMetaApiResponse {
  title: string;
  description: string;
  canonical: string | null;
  robots: string;
  ogImage: string | null;
  ogImageAlt: string | null;
  jsonLd: string[] | null;
  notFound: boolean;
}

/**
 * Raw response from GET /api/public/seo/config.
 * Matches backend PublicSeoConfigResponse.
 */
export interface PublicSeoConfigApiResponse {
  publicSiteUrl: string | null;
  defaultSeoTitle: string | null;
  defaultMetaDescription: string | null;
  defaultSocialImageUrl: string | null;
  businessName: string | null;
  logoUrl: string | null;
  whatsapp: string | null;
  address: string | null;
  hours: string | null;
  email: string | null;
  instagram: string | null;
  country: string | null;
  region: string | null;
  city: string | null;
  postalCode: string | null;
  googleSiteVerification: string | null;
  bingSiteVerification: string | null;
}

export function mapSeoPageMeta(api: SeoPageMetaApiResponse): SeoPageMeta {
  return {
    title: api.title,
    description: api.description,
    canonicalUrl: api.canonical,
    robots: api.robots || 'index,follow',
    ogTitle: api.title,
    ogDescription: api.description,
    ogImage: api.ogImage,
    ogImageAlt: api.ogImageAlt,
    ogType: 'website',
    jsonLd: api.jsonLd ?? [],
    statusCode: api.notFound ? 404 : undefined,
  };
}
