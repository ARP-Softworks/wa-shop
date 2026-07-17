package uy.washop.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import uy.washop.config.AppProperties;

class MercadoPagoPaymentProviderTest {

    private static final String SECRET = "test-webhook-secret";

    private MercadoPagoPaymentProvider provider() {
        AppProperties props = new AppProperties();
        props.getPayment().getMercadoPago().setAccessToken("TEST-access-token");
        props.getPayment().getMercadoPago().setPublicKey("TEST-public-key");
        props.getPayment().getMercadoPago().setWebhookSecret(SECRET);
        return new MercadoPagoPaymentProvider(props);
    }

    private String signManifest(String dataId, String requestId, String ts, String secret) throws Exception {
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + requestId + ";ts:" + ts + ";";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] computed = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : computed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void acceptsValidSignature() throws Exception {
        String dataId = "123456789";
        String requestId = "req-1";
        String ts = "1704908010";
        String v1 = signManifest(dataId, requestId, ts, SECRET);
        String header = "ts=" + ts + ",v1=" + v1;

        assertThat(provider().verifyWebhookSignature(header, requestId, dataId)).isTrue();
    }

    @Test
    void rejectsTamperedSignature() throws Exception {
        String dataId = "123456789";
        String requestId = "req-1";
        String ts = "1704908010";
        String v1 = signManifest(dataId, requestId, ts, "wrong-secret");
        String header = "ts=" + ts + ",v1=" + v1;

        assertThat(provider().verifyWebhookSignature(header, requestId, dataId)).isFalse();
    }

    @Test
    void rejectsWhenDataIdDiffersFromSignedValue() throws Exception {
        String requestId = "req-1";
        String ts = "1704908010";
        String v1 = signManifest("123456789", requestId, ts, SECRET);
        String header = "ts=" + ts + ",v1=" + v1;

        assertThat(provider().verifyWebhookSignature(header, requestId, "999999999")).isFalse();
    }

    @Test
    void rejectsMissingOrMalformedHeader() {
        assertThat(provider().verifyWebhookSignature(null, "req-1", "123")).isFalse();
        assertThat(provider().verifyWebhookSignature("garbage", "req-1", "123")).isFalse();
        assertThat(provider().verifyWebhookSignature("ts=1,v1=", "req-1", "123")).isFalse();
    }

    @Test
    void rejectsWhenWebhookSecretNotConfigured() {
        AppProperties props = new AppProperties();
        props.getPayment().getMercadoPago().setAccessToken("TEST-access-token");
        props.getPayment().getMercadoPago().setPublicKey("TEST-public-key");
        MercadoPagoPaymentProvider provider = new MercadoPagoPaymentProvider(props);

        assertThat(provider.verifyWebhookSignature("ts=1,v1=abc", "req-1", "123")).isFalse();
    }
}
