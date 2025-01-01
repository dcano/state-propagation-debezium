package io.twba.tk.security;

import lombok.Data;

@Data
public class MtlsClient {

    private String keyStorePath;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
    private String trustStorePath;
    private String trustStorePassword;

}
