package com.nexthink.intern.automation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String playbook;
    private String target;
    private String isSuccess;
    public String getStatus() {
        return isSuccess;
    }

    public void setStatus(String isSuccess) {
        this.isSuccess = isSuccess;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaybook() {
        return playbook;
    }

    public void setPlaybook(String playbook) {
        this.playbook = playbook;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}