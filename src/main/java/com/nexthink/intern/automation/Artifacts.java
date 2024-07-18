package com.nexthink.intern.automation;

import java.util.HashMap;
import java.util.Map;

public class Artifacts {

    private String type;
    private Map<String, String> serverValues;

    public Artifacts() {
        this.serverValues = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addServerValue(String serverName, String value) {
        serverValues.put(serverName, value);
    }

    public String getValueForServer(String serverName) {
        return serverValues.getOrDefault(serverName, "No value");
    }

    // Optional: If you want to retrieve all values for all servers
}
