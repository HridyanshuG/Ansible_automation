package com.nexthink.intern.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.stereotype.Component;
import org.ini4j.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component

public class AnsibleInventoryReader {
    public static List<String> getHostsFromInventory(String inventoryFile) throws IOException {
        String playbookPath = System.getenv("playbookPath");
        String inventoryFilePath = playbookPath + "/inventory/" + inventoryFile;
        File inventoryFileActual = new File(inventoryFilePath);
        List<String> hosts = new ArrayList<>();

        Ini ini = new Ini(inventoryFileActual);

        // Get the "servers" section
        Profile.Section serversSection = ini.get("servers");

        if (serversSection != null) {
            for (String serverName : serversSection.keySet()) {
                String[] parts = serverName.split("\\s+", 2);
                hosts.add(parts[0]);
            }
        }
        return hosts;

    }
}
