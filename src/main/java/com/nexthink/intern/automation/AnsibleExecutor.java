package com.nexthink.intern.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


@Component
public class AnsibleExecutor {

    @Autowired
    JobRepository jobRepository;

    public void Testjob(){
        Job job = new Job();
        job.setPlaybook("TEST@");
        jobRepository.save(job);

    }

    /**
     * /run the playbook via command line and check the standard output result and set the status
     *
     * @param playbookName
     * @param serverName
     * @return
     */
    public ExecuteResult execute(String playbookName, String serverName) throws IOException, InterruptedException {

        String playbookPath = System.getenv("playbookPath");
        if (playbookPath == null) {
            throw new IllegalArgumentException("System property 'playbookPath' is not set");
        }

        //prepending the folder playbooks and creating a new name to access it form the resource folder
        String playbookNametemp = playbookPath + "/playbook/" + playbookName;
        String servers1 = playbookPath + "/inventory/" + serverName;
        // Get playbook file from resources folder
        File playbook1 = new File(playbookNametemp);
        File inventory1 = new File(servers1);
        //String servers1 = "/Users/hridyanshu.ghura/Desktop/portalautomation/inventory/" + serverName;
        //File playbook = new ClassPathResource(playbookNametemp).getFile();
        //File inventory1 = new ClassPathResource(serverName).getFile();

        // Construct the command
        String command = String.format("ansible-playbook %s -i %s", playbook1.getAbsolutePath(), inventory1.getAbsolutePath());
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        Scanner standardOutputScanner = new Scanner(process.getInputStream());
        Scanner standardErrorScanner = new Scanner(process.getErrorStream());
        StringBuilder output = new StringBuilder();
        while (standardOutputScanner.hasNextLine()) {
            output.append(standardOutputScanner.nextLine()).append("\n");
        }

        StringBuilder errorOutput = new StringBuilder();
        while (standardErrorScanner.hasNextLine()) {
            errorOutput.append(standardErrorScanner.nextLine()).append("\n");
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Playbook execution successful! Output:\n" + output.toString());
        } else {
            System.err.println("Playbook execution failed (exit code: " + exitCode + "). Error:\n" + errorOutput.toString());
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

