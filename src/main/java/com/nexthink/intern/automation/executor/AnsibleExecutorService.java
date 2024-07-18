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

import java.io.*;
import java.util.ArrayList;
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
    public void submitJob(Job savedJob,String jmcRunningTime) {
        executorService.submit(() -> {
            try {
                ExecuteResult result = execute(savedJob,jmcRunningTime);

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
    public ExecuteResult execute(Job job, String jmcRunningTime) {

        StringBuilder output = new StringBuilder();
        int exitCode = 0;
        try{
            //prepending the folder playbooks and creating a new name to access it form the resource folder
            String playbookNametemp = ansibleEnv.getRootPath() + "/playbook/" + job.getPlaybook();
            String servers1 = ansibleEnv.getRootPath() + "/inventory/inventory.ini";
            // Get playbook file from resources folder
            File playbook1 = new File(playbookNametemp);
            File inventory1 = new File(servers1);
            String resultsDirPath = ansibleEnv.getRootPath() + "/Result/" + job.getId();
            File resultsDir = new File(resultsDirPath);
            if (!resultsDir.exists()) {
                boolean wasDirectoryMade = resultsDir.mkdirs();
                if (!wasDirectoryMade) {
                    logger.error("Failed to create directory for job results: " + resultsDirPath);
                    // Consider how you want to handle this failure. For now, just logging.
                }
            }
            List<String> Variables= new ArrayList<>();
            Variables.add("Job_id=" + job.getId());
            Variables.add("PlaybookPath=" + ansibleEnv.getRootPath());
            Variables.add("JMCRunningTime=" + jmcRunningTime);
            // ansible-playbook playbook name -i inventory file --extra-vars "
            String VariablesString = String.join(" ", Variables);

            // Construct the command
            String command = String.format("ansible-playbook %s -i %s --limit %s --extra-vars \"%s\"", playbook1.getAbsolutePath(), inventory1.getAbsolutePath(), job.getListOfTargets(),VariablesString);
            logger.info("Executing the playbook with command: " + command);
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            String OutputFilePath = resultsDirPath + "/output.txt";
            String ErrorFilePath = resultsDirPath + "/error.txt";
            try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(OutputFilePath));
                 BufferedWriter errorWriter = new BufferedWriter(new FileWriter(ErrorFilePath))) {
                // Write headers to both files
                outputWriter.write("Playbook Execution Output:\n");
                errorWriter.write("Playbook Execution Error:\n");

                // Start threads to read the output and error streams
                readStream(process.getInputStream(), outputWriter);
                readStream(process.getErrorStream(), errorWriter);

                // Wait for the process to complete
                exitCode = process.waitFor();
                logger.info("Process exited with code: " + exitCode);

                // Note: The streams are closed by the try-with-resources statement
            } catch (IOException | InterruptedException e) {
                logger.error("Failed to execute the process", e);
            }

            exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Playbook execution successful! Output:\n" + output);
            } else {
                System.err.println("Playbook execution failed (exit code: " + exitCode + ")! Output:\n" + output);
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
    private void readStream(InputStream inputStream, BufferedWriter writer) {
        new Thread(() -> {
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    writer.write(line + "\n");
                    writer.flush();
                }
            } catch (IOException e) {
                logger.error("Error processing stream", e);
            }
        }).start();
    }
}

