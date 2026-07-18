package uy.washop.order.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.washop.config.AppProperties;
import uy.washop.shared.exception.RateLimitExceededException;

class CheckoutRateLimitServiceTest {

    private CheckoutRateLimitService service;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getOrder().setCheckoutIpMaxPerWindow(2);
        props.getOrder().setCheckoutPhoneMaxPerWindow(2);
        props.getOrder().setCheckoutWindowSeconds(900);
        service = new CheckoutRateLimitService(props);
    }

    @Test
    void allowsUntilLimitThenBlocksByIp() {
        assertThatCode(() -> service.assertAllowed("1.1.1.1", "099111111")).doesNotThrowAnyException();
        service.recordAttempt("1.1.1.1", "099111111");
        assertThatCode(() -> service.assertAllowed("1.1.1.1", "099222222")).doesNotThrowAnyException();
        service.recordAttempt("1.1.1.1", "099222222");

        assertThatThrownBy(() -> service.assertAllowed("1.1.1.1", "099333333"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void blocksByPhoneAcrossDifferentIps() {
        service.recordAttempt("1.1.1.1", "099999999");
        service.recordAttempt("2.2.2.2", "099999999");

        assertThatThrownBy(() -> service.assertAllowed("3.3.3.3", "099999999"))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
