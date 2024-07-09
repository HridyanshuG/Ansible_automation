package com.nexthink.intern.automation.executor;

import com.nexthink.intern.automation.ExecuteResult;
import com.nexthink.intern.automation.Job;

import java.io.IOException;

public interface AnsibleExecutor {
    void submitJob(Job savedJob);

    ExecuteResult execute(String playbookName, String ListOfTargets) throws IOException, InterruptedException;
}