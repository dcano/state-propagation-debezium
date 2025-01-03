package io.twba.rating_system.config;

import io.twba.tk.security.MtlsClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

@Configuration
public class MtlsClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MtlsClientConfig.class);
    private static final String KEYSTORE_TYPE = "JKS";

    @Bean
    @ConfigurationProperties(prefix = "twba.mtls-client")
    public MtlsClientProperties mtlsClient() {
        return new MtlsClientProperties();
    }

    @Bean
    public RestTemplate mtlsRestTemplate(@Autowired MtlsClientProperties mtlsClientProperties)  {
        SSLContext sslContext = configureSSLContext(mtlsClientProperties);
        if(Objects.nonNull(sslContext)) {
            return new RestTemplate(createRequestFactory(sslContext));
        }
        return new RestTemplate();
    }

    private SSLContext configureSSLContext(MtlsClientProperties mtlsClientProperties) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            char[] keyStorePassword = mtlsClientProperties.getKeyStorePassword().toCharArray();
            keyStore.load(new FileInputStream(mtlsClientProperties.getKeyStorePath()), keyStorePassword);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);

            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            trustStore.load(new FileInputStream(mtlsClientProperties.getTrustStorePath()), mtlsClientProperties.getTrustStorePassword().toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.warn("Error configuring SSLContext, falling back to non-secured rest client", e);
            return null;
        }
    }

    private ClientHttpRequestFactory createRequestFactory(SSLContext sslContext) {
        return new CustomRequestFactory(sslContext);
    }

    private static class CustomRequestFactory extends SimpleClientHttpRequestFactory {

        private final SSLContext sslContext;

        public CustomRequestFactory(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
            if (connection instanceof javax.net.ssl.HttpsURLConnection) {
                ((javax.net.ssl.HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

}
