import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BreadcrumbsComponent } from '../../../shared/components/breadcrumbs/breadcrumbs.component';

interface FaqItem {
  question: string;
  answer: string;
}

/** Mirrors backend uy.washop.seo.application.FaqContent */
const FAQ_ITEMS: FaqItem[] = [
  {
    question: '¿WA Shop vende iPhone nuevos y usados?',
    answer:
      'Sí. En WA Shop encontrás iPhone nuevos y usados seleccionados, además de accesorios y servicio técnico. Cada equipo publicado indica condición, capacidad y precio en Uruguay.',
  },
  {
    question: '¿Cómo consulto por un producto?',
    answer:
      'Podés abrir el detalle del producto en el sitio y escribirnos por WhatsApp. Te respondemos con disponibilidad, estado del equipo y formas de pago o retiro.',
  },
  {
    question: '¿Ofrecen garantía en equipos usados?',
    answer:
      'Los usados publicados suelen incluir una garantía comercial indicada en la ficha del producto. El plazo y la cobertura exactos están detallados en cada publicación.',
  },
  {
    question: '¿Hacen servicio técnico de iPhone?',
    answer:
      'Sí. Realizamos servicios como cambio de batería, reparación de pantalla y otros trabajos habituales. Consultá el listado de servicios o escribinos por WhatsApp con el modelo y el problema.',
  },
  {
    question: '¿Dónde están ubicados?',
    answer:
      'Atendemos en Uruguay. La dirección, horarios y datos de contacto actualizados figuran en la página de contacto y en la configuración pública del sitio.',
  },
  {
    question: '¿WA Shop es un servicio oficial de Apple?',
    answer:
      'No. WA Shop es un comercio independiente de venta de iPhone, accesorios y servicio técnico en Uruguay. No somos un Apple Store ni un proveedor autorizado de Apple.',
  },
];

@Component({
  selector: 'app-faq-page',
  standalone: true,
  imports: [RouterLink, BreadcrumbsComponent],
  templateUrl: './faq-page.component.html',
  styleUrl: './faq-page.component.scss',
})
export class FaqPageComponent {
  readonly items = FAQ_ITEMS;
  readonly crumbs = [
    { label: 'Inicio', link: '/' },
    { label: 'Preguntas frecuentes' },
  ];
}
