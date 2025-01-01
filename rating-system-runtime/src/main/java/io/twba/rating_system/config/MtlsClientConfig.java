package io.twba.rating_system.config;

import io.twba.tk.security.MtlsClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MtlsClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "twba.mtls-client")
    public MtlsClient mtlsClient() {
        return new MtlsClient();
    }

}
