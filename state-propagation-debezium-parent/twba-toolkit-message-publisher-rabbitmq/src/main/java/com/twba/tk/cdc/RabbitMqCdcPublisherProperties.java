package com.twba.tk.cdc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RabbitMqCdcPublisherProperties {

    private String exchange;

}
