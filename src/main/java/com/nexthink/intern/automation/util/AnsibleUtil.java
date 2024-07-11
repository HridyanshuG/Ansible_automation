package com.nexthink.intern.automation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AnsibleUtil {

    @Autowired
    private AnsibleEnv ansibleEnv;

    public void createExecutionFolder(Long id) throws IOException {
        Files.createDirectories(getExecutionPath(id));
    }

    public void createExecutionOutFile(Long id) throws IOException {
        Files.createFile(getAnsibleOutPath(id));
    }

    public Path getExecutionPath(Long id){
        return Paths.get(ansibleEnv.getRootPath(), "execution",id.toString());
    }

    public Path getAnsibleOutPath(Long id){
        return Paths.get(ansibleEnv.getRootPath(), "execution", id.toString(), "out.txt");
    }
}
