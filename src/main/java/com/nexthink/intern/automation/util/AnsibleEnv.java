package com.nexthink.intern.automation.util;

import org.springframework.stereotype.Component;

@Component
public class AnsibleEnv {

    private static String rootPath;

    public static String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
