package com.nexthink.intern.automation.executor;

import com.nexthink.intern.automation.ExecuteResult;
import com.nexthink.intern.automation.Job;
import com.nexthink.intern.automation.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("simulate")
public class AnsibleTestExecutor implements AnsibleExecutor {

    private Logger logger = LoggerFactory.getLogger(AnsibleTestExecutor.class);

    @Autowired
    private JobRepository jobRepository;


    @Override
    public void submitJob(Job savedJob) {
        logger.info("test submitjob invoked");
        savedJob.setStatus("success");
        jobRepository.save(savedJob);
    }

    @Override
    public ExecuteResult execute(String playbookName, String ListOfTargets) throws IOException, InterruptedException {
        logger.info("test execute");
        return new ExecuteResult();
    }
}
