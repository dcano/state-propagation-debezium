package com.twba.tk.cdc;

import lombok.Data;

@Data
public class AmqpProperties {

    public String hostName;
    private int port;
    private String username;
    private String password;

}
