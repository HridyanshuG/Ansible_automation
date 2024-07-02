package com.nexthink.intern.automation;

import java.util.List;

public class JobResult {

    private Long id;
    private String status;
    private List<Artifacts> artifacts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Artifacts> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifacts> artifacts) {
        this.artifacts = artifacts;
    }
}
