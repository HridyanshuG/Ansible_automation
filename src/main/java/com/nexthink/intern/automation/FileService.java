package com.nexthink.intern.automation;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileService {

    public List<String> getFileNames(String folderPath) {
        try {
            return Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading files from directory", e);
        }
    }
}