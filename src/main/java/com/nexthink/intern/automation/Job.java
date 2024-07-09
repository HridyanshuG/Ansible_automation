package com.nexthink.intern.automation;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String playbook;
    private String target;
    private String isSuccess;
    private LocalDateTime startTime;
    private String ListOfTargets;


    @PrePersist
    public void prePersist() {
        startTime = LocalDateTime.now();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
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

    public String getListOfTargets() {
        return ListOfTargets;
    }

    public void setListOfTargets(String additionalInfo) {
        this.ListOfTargets = additionalInfo;
    }
}