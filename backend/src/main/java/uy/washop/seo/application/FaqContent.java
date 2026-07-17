package uy.washop.seo.application;

import java.util.List;

/**
 * Real FAQ content for WA Shop (Uruguay). Honest business answers — no fake Apple official claims.
 */
public final class FaqContent {

    public record FaqItem(String question, String answer) {
    }

    private static final List<FaqItem> ITEMS = List.of(
            new FaqItem(
                    "¿WA Shop vende iPhone nuevos y usados?",
                    "Sí. En WA Shop encontrás iPhone nuevos y usados seleccionados, además de accesorios y servicio técnico. "
                            + "Cada equipo publicado indica condición, capacidad y precio en Uruguay."
            ),
            new FaqItem(
                    "¿Cómo consulto por un producto?",
                    "Podés abrir el detalle del producto en el sitio y escribirnos por WhatsApp. "
                            + "Te respondemos con disponibilidad, estado del equipo y formas de pago o retiro."
            ),
            new FaqItem(
                    "¿Ofrecen garantía en equipos usados?",
                    "Los usados publicados suelen incluir una garantía comercial indicada en la ficha del producto. "
                            + "El plazo y la cobertura exactos están detallados en cada publicación."
            ),
            new FaqItem(
                    "¿Hacen servicio técnico de iPhone?",
                    "Sí. Realizamos servicios como cambio de batería, reparación de pantalla y otros trabajos habituales. "
                            + "Consultá el listado de servicios o escribinos por WhatsApp con el modelo y el problema."
            ),
            new FaqItem(
                    "¿Dónde están ubicados?",
                    "Atendemos en Uruguay. La dirección, horarios y datos de contacto actualizados figuran en la página de contacto "
                            + "y en la configuración pública del sitio."
            ),
            new FaqItem(
                    "¿WA Shop es un servicio oficial de Apple?",
                    "No. WA Shop es un comercio independiente de venta de iPhone, accesorios y servicio técnico en Uruguay. "
                            + "No somos un Apple Store ni un proveedor autorizado de Apple."
            )
    );

    private FaqContent() {
    }

    public static List<FaqItem> items() {
        return ITEMS;
    }
}
