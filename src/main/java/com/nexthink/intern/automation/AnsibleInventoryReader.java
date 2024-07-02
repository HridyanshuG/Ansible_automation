package com.nexthink.intern.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component

public class AnsibleInventoryReader {
    public static List<String> getHostsFromInventory(String inventoryFile) {
        String playbookPath = System.getenv("playbookPath");
        String inventoryFilePath = playbookPath + "/inventory/" + inventoryFile;

        List<String> hosts = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ansible-inventory", "--list", "-i", inventoryFilePath);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output.toString());

            jsonNode.fieldNames().forEachRemaining(hosts::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hosts;
    }
}
