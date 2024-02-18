package com.twba.tk.cdc;

import lombok.Data;

import java.util.Map;

@Data
public class DebeziumProperties {

    private String connectorClass;
    private OffsetProperties offsetStorage;
    private SourceDatabaseProperties sourceDatabaseProperties;


    @Data
    public static class OffsetProperties {
        private String type;
        private Map<String, String> offsetProps;

    }
    @Data
    public static class SourceDatabaseProperties {
        private String hostname;
        private String user;
        private String password;
        private String dbName;
        private String includeList;
        private String serverId;
        private String serverName;
        private String historyType;
        private Map<String, String> historyProps;

    }
}
