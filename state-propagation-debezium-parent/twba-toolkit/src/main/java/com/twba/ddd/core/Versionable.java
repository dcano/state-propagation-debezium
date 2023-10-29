package com.twba.ddd.core;

public interface Versionable {

    void setVersion(ApplicationVersion applicationVersion);
    ApplicationVersion getVersion();

}
