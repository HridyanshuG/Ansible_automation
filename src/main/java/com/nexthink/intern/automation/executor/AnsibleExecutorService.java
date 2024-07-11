package com.nexthink.intern.automation.executor;

import com.nexthink.intern.automation.*;
import com.nexthink.intern.automation.util.AnsibleEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Profile("prod")
public class AnsibleExecutorService implements AnsibleExecutor {

    private Logger logger = LoggerFactory.getLogger(AnsibleExecutorService.class);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AnsibleEnv ansibleEnv;

    @Override
    public void submitJob(Job savedJob) {
        executorService.submit(() -> {
            try {
                ExecuteResult result = execute(savedJob);

                if(result.isSuccess()) {
                    savedJob.setStatus("success");
                }
                else {
                    savedJob.setStatus("failed");
                }
                jobRepository.save(savedJob);
            } catch (Exception e) {
                savedJob.setStatus("failed");
                jobRepository.save(savedJob);
                logger.error(e.getMessage(),e);
            }
        });
    }

    @Override
    public ExecuteResult execute(Job job) {

        StringBuilder output = new StringBuilder();
        int exitCode = 0;
        try{
            //prepending the folder playbooks and creating a new name to access it form the resource folder
            String playbookNametemp = ansibleEnv.getRootPath() + "/playbook/" + job.getPlaybook();
            String servers1 = ansibleEnv + "/inventory/inventory.ini";
            // Get playbook file from resources folder
            File playbook1 = new File(playbookNametemp);
            File inventory1 = new File(servers1);
            List<String> databases = Arrays.asList("db1", "db3");
            // ansible-playbook playbook name -i inventory file --extra-vars "
            String ServerNames = String.join(",", databases);

            // Construct the command
            String command = String.format("ansible-playbook %s -i %s --limit %s", playbook1.getAbsolutePath(), inventory1.getAbsolutePath(), job.getListOfTargets());

            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            Scanner standardOutputScanner = new Scanner(process.getInputStream());
            Scanner standardErrorScanner = new Scanner(process.getErrorStream());
            while (standardOutputScanner.hasNextLine()) {
                output.append(standardOutputScanner.nextLine()).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while (standardErrorScanner.hasNextLine()) {
                errorOutput.append(standardErrorScanner.nextLine()).append("\n");
            }

            exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Playbook execution successful! Output:\n" + output.toString());
            } else {
                System.err.println("Playbook execution failed (exit code: " + exitCode + "). Error:\n" + errorOutput.toString());
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return parseResult(output.toString(), exitCode);

    }

    private ExecuteResult parseResult(String output, int exitCode) {
        ExecuteResult re = new ExecuteResult();
        if(exitCode == 0) {
          re.setSuccess(true);
        } else {
            re.setSuccess(false);
        }
        //re.setSuccess(true);
        return re;
    }
}

