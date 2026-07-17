package uy.washop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Session session = new Session();
    private final Cookie cookie = new Cookie();
    private final Login login = new Login();
    private final AdminBootstrap adminBootstrap = new AdminBootstrap();
    private final Whatsapp whatsapp = new Whatsapp();
    private final Media media = new Media();
    private final Payment payment = new Payment();
    private final Order order = new Order();
    private final Seo seo = new Seo();
    private final Analytics analytics = new Analytics();
    private String publicBaseUrl = "http://localhost:8080";
    private String publicSiteUrl = "";

    public Cors getCors() {
        return cors;
    }

    public Session getSession() {
        return session;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public Login getLogin() {
        return login;
    }

    public AdminBootstrap getAdminBootstrap() {
        return adminBootstrap;
    }

    public Whatsapp getWhatsapp() {
        return whatsapp;
    }

    public Media getMedia() {
        return media;
    }

    public Payment getPayment() {
        return payment;
    }

    public Order getOrder() {
        return order;
    }

    public Seo getSeo() {
        return seo;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getPublicSiteUrl() {
        return publicSiteUrl;
    }

    public void setPublicSiteUrl(String publicSiteUrl) {
        this.publicSiteUrl = publicSiteUrl;
    }

    public static class Cors {
        private boolean enabled = false;
        private String allowedOrigins = "http://localhost:4200";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Session {
        /** Session idle timeout in seconds. */
        private int timeoutSeconds = 28_800;

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    public static class Cookie {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String sessionName = "WASESSION";

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }

        public String getSessionName() {
            return sessionName;
        }

        public void setSessionName(String sessionName) {
            this.sessionName = sessionName;
        }
    }

    public static class Login {
        private int maxFailedAttempts = 5;
        private int lockDurationSeconds = 900;

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public void setMaxFailedAttempts(int maxFailedAttempts) {
            this.maxFailedAttempts = maxFailedAttempts;
        }

        public int getLockDurationSeconds() {
            return lockDurationSeconds;
        }

        public void setLockDurationSeconds(int lockDurationSeconds) {
            this.lockDurationSeconds = lockDurationSeconds;
        }
    }

    public static class AdminBootstrap {
        private String email = "";
        private String password = "";

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Whatsapp {
        private String phone = "";

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class Media {
        private String provider = "local";
        private long maxFileSize = 5_242_880L;
        private String allowedTypes = "image/jpeg,image/png,image/webp";
        private final Cloudinary cloudinary = new Cloudinary();
        private final Local local = new Local();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String getAllowedTypes() {
            return allowedTypes;
        }

        public void setAllowedTypes(String allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        public Cloudinary getCloudinary() {
            return cloudinary;
        }

        public Local getLocal() {
            return local;
        }

        public static class Cloudinary {
            private String cloudName = "";
            private String apiKey = "";
            private String apiSecret = "";

            public String getCloudName() {
                return cloudName;
            }

            public void setCloudName(String cloudName) {
                this.cloudName = cloudName;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getApiSecret() {
                return apiSecret;
            }

            public void setApiSecret(String apiSecret) {
                this.apiSecret = apiSecret;
            }
        }

        public static class Local {
            private String baseDir = "./data/media";

            public String getBaseDir() {
                return baseDir;
            }

            public void setBaseDir(String baseDir) {
                this.baseDir = baseDir;
            }
        }
    }

    public static class Payment {
        private String provider = "mercadopago";
        private final MercadoPago mercadoPago = new MercadoPago();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public MercadoPago getMercadoPago() {
            return mercadoPago;
        }

        public static class MercadoPago {
            private String accessToken = "";
            private String publicKey = "";
            private String webhookSecret = "";

            public String getAccessToken() {
                return accessToken;
            }

            public void setAccessToken(String accessToken) {
                this.accessToken = accessToken;
            }

            public String getPublicKey() {
                return publicKey;
            }

            public void setPublicKey(String publicKey) {
                this.publicKey = publicKey;
            }

            public String getWebhookSecret() {
                return webhookSecret;
            }

            public void setWebhookSecret(String webhookSecret) {
                this.webhookSecret = webhookSecret;
            }
        }
    }

    public static class Order {
        /** Minutes an unpaid order can stay PENDING_PAYMENT before its stock reservation is released. */
        private int pendingExpiryMinutes = 45;

        public int getPendingExpiryMinutes() {
            return pendingExpiryMinutes;
        }

        public void setPendingExpiryMinutes(int pendingExpiryMinutes) {
            this.pendingExpiryMinutes = pendingExpiryMinutes;
        }
    }

    public static class Seo {
        private String googleSiteVerification = "";
        private String bingSiteVerification = "";

        public String getGoogleSiteVerification() {
            return googleSiteVerification;
        }

        public void setGoogleSiteVerification(String googleSiteVerification) {
            this.googleSiteVerification = googleSiteVerification;
        }

        public String getBingSiteVerification() {
            return bingSiteVerification;
        }

        public void setBingSiteVerification(String bingSiteVerification) {
            this.bingSiteVerification = bingSiteVerification;
        }
    }

    public static class Analytics {
        private String googleAnalyticsId = "";
        private String googleTagManagerId = "";

        public String getGoogleAnalyticsId() {
            return googleAnalyticsId;
        }

        public void setGoogleAnalyticsId(String googleAnalyticsId) {
            this.googleAnalyticsId = googleAnalyticsId;
        }

        public String getGoogleTagManagerId() {
            return googleTagManagerId;
        }

        public void setGoogleTagManagerId(String googleTagManagerId) {
            this.googleTagManagerId = googleTagManagerId;
        }
    }
}
