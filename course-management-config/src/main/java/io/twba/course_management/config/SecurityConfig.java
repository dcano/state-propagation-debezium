package io.twba.course_management.config;

import io.twba.tk.security.AllowedService;
import io.twba.tk.security.ServiceAuthenticator;
import io.twba.tk.security.ServiceAuthenticatorPropertyBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Profile("!unsafe")
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ServiceAuthenticator serviceAuthenticator) throws Exception {

        http.x509(x509 -> x509.subjectPrincipalRegex("CN=(.*?)(?:,|$)").userDetailsService(userDetailsService(serviceAuthenticator)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest()
                        .authenticated()

                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(ServiceAuthenticator serviceAuthenticator) {
        return username -> {
            if (serviceAuthenticator.isAllowed(username)) {
                return new User(username, "", AuthorityUtils.createAuthorityList(serviceAuthenticator.roles(username)));
            }
            throw new UsernameNotFoundException("User not found!");
        };
    }

    @Bean
    public ServiceAuthenticator serviceAuthenticator(@Autowired List<AllowedService> allowedServices) {
        LOGGER.info("ServiceAuthenticator configured for services [{}] with roles [{}] ", allowedServices.stream().map(AllowedService::getServiceName).collect(Collectors.joining(",")), allowedServices.stream().flatMap(s -> s.getRoles().stream()).collect(Collectors.joining(",")));
        return new ServiceAuthenticatorPropertyBased(allowedServices);
    }

    @ConfigurationProperties(prefix = "twba.allowed-services")
    @Bean
    public List<AllowedService> allowedServices() {
        return new ArrayList<>();
    }


}
