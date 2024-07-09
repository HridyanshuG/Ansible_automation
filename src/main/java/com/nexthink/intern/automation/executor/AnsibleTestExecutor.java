package com.nexthink.intern.automation.executor;

import com.nexthink.intern.automation.ExecuteResult;
import com.nexthink.intern.automation.Job;
import com.nexthink.intern.automation.JobRepository;
import com.nexthink.intern.automation.util.AnsibleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Profile("simulate")
public class AnsibleTestExecutor implements AnsibleExecutor {

    private Logger logger = LoggerFactory.getLogger(AnsibleTestExecutor.class);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AnsibleUtil ansibleUtil;


    @Override
    public void submitJob(Job savedJob)  {
        logger.info("test submitjob invoked");
        executorService.submit(() ->{
            try {
                preExecute(savedJob);
                execute(savedJob);
                savedJob.setStatus("success");
                jobRepository.save(savedJob);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ExecuteResult execute(Job job) {
        try{
            logger.info("test execute123");
            Path outPath = ansibleUtil.getAnsibleOutPath(job.getId());
            for(int i=0;i<100;i++){
                Files.writeString(outPath,"execution "+i+"\n", StandardOpenOption.APPEND);
                Thread.sleep(1000);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return new ExecuteResult();
    }

    public void preExecute(Job job) throws IOException {
        ansibleUtil.createExecutionFolder(job.getId());
        ansibleUtil.createExecutionOutFile(job.getId());
    }

    public void postExecute(Job job){

    }


}
