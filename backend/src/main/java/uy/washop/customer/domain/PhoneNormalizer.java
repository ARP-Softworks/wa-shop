package uy.washop.customer.domain;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    /** Digits-only form used for dedupe matching — mirrors frontend WhatsappLinkService normalization. */
    public static String normalize(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        return rawPhone.replaceAll("\\D", "");
    }
}
