import { Pipe, PipeTransform } from '@angular/core';

const ENTITY_LABELS: Record<string, string> = {
  Product: 'Producto',
  ProductVariant: 'Variante de producto',
  Category: 'Categoría',
  TechnicalService: 'Servicio técnico',
  SiteSettings: 'Configuración del sitio',
  Order: 'Pedido',
  Customer: 'Cliente',
  DiscountCode: 'Código de descuento',
  Promotion: 'Promoción',
  HeroBanner: 'Banner',
  Media: 'Imagen',
  User: 'Usuario',
  Inquiry: 'Consulta',
};

@Pipe({ name: 'auditEntityLabel', standalone: true })
export class AuditEntityLabelPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    return ENTITY_LABELS[value] ?? value;
  }
}
