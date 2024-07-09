package com.nexthink.intern.automation;

import org.springframework.stereotype.Component;

@Component
public class AnsibleEnv {

    private String rootPath;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
