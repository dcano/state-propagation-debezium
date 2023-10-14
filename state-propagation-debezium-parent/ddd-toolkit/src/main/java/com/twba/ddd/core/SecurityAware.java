package com.twba.ddd.core;

interface SecurityAware {

    void setUserInfo(DomainUser domainUser);
    DomainUser extractUserInfo();
    void setSecurityToken(String securityToken);
    String getSecurityToken();

}
