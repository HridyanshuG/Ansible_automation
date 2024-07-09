package com.nexthink.intern.automation;

import java.util.List;

public class ExecutionForm {
    private String playbook;
    private List<String> targets;

    // Getters and setters
    public String getPlaybook() {
        return playbook;
    }

    public void setPlaybook(String playbook) {
        this.playbook = playbook;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
}
