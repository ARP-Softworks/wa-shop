package uy.washop.inquiry.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import uy.washop.config.ApplicationConfig;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquirySource;
import uy.washop.inquiry.domain.InquiryStatus;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.shared.domain.CurrencyCode;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan("uy.washop")
@EnableJpaRepositories("uy.washop")
@Import(ApplicationConfig.class)
class InquiryRepositoryTest {

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void linksOptionalProductAndFiltersByStatus() {
        Product product = new Product();
        product.setSlug("inq-product");
        product.setName("Producto consulta");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.NEW);
        product.setPrice(new BigDecimal("1000.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        product.setPublished(true);
        product = productRepository.saveAndFlush(product);

        Inquiry inquiry = new Inquiry();
        inquiry.setCustomerName("Ana");
        inquiry.setPhone("59891234567");
        inquiry.setEmail("ana@example.com");
        inquiry.setMessage("¿Sigue disponible?");
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.NEW);
        inquiry.setSource(InquirySource.FORM);
        inquiryRepository.saveAndFlush(inquiry);

        assertThat(inquiryRepository.findByStatusOrderByCreatedAtDesc(InquiryStatus.NEW))
                .hasSize(1)
                .first()
                .extracting(item -> item.getProduct().getId())
                .isEqualTo(product.getId());
    }
}
