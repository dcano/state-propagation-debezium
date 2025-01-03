package io.twba.rating_system.config;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("sslbundle")
public class MtlsBundleConfig {

    private static final String CLIENT_BUNDLE = "client-bundle";
    private static final String CA_BUNDLE = "ca-bundle";

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {
        return restTemplateBuilder.sslBundle(sslBundles.getBundle(CLIENT_BUNDLE)).build();
    }

}
