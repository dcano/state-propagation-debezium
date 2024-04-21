package com.twba.tk.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Created by alonsotd on 10/10/2017.
 */

@Getter
public abstract class MultiTenantEntity extends Entity {

    @NotNull(message = "lblTenantIdNotNull")
    @Valid
    private final TenantId tenantId;

    public MultiTenantEntity(TenantId tenantId, long version) {
        super(version);
        this.tenantId = tenantId;
        this.validateProperty("tenantId");
    }

}
