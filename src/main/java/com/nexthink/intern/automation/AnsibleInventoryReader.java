package com.nexthink.intern.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexthink.intern.automation.util.AnsibleEnv;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ini4j.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component

public class AnsibleInventoryReader {
    @Autowired
    private static AnsibleEnv ansibleEnv;
    public static List<String> getHostsFromInventory(String inventoryFile) throws IOException {
        String playbookPath = ansibleEnv.getRootPath();
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
