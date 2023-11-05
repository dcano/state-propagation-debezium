package com.twba.tk.core;

import com.twba.tk.security.SecurityProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ApplicationProperties {

    private String uri;
    private String context;
    private String labels;
    private List<String> allowedOrigins;
    private List<String> whitelistedUrls;
    private SecurityProperties security;
    private String serviceName;
    private boolean profiling;
    private String protocol;

    public String protocol() {
        return (Objects.isNull(protocol) || protocol.isEmpty())?"http://":protocol;
    }


}
