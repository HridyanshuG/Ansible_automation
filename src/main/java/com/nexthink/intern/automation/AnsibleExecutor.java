package com.nexthink.intern.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


@Component
public class AnsibleExecutor {

    @Autowired
    JobRepository jobRepository;

    public ExecuteResult execute(String playbookName, String ListOfTargets) throws IOException, InterruptedException {

        String playbookPath = System.getenv("playbookPath");
        if (playbookPath == null) {
            throw new IllegalArgumentException("System property 'playbookPath' is not set");
        }

        //prepending the folder playbooks and creating a new name to access it form the resource folder
        String playbookNametemp = playbookPath + "/playbook/" + playbookName;
        String servers1 = playbookPath + "/inventory/inventory.ini";
        // Get playbook file from resources folder
        File playbook1 = new File(playbookNametemp);
        File inventory1 = new File(servers1);
        List<String> databases = Arrays.asList("db1", "db3");
        // ansible-playbook playbook name -i inventory file --extra-vars "
        String ServerNames = String.join(",", databases);

        // Construct the command
        String command = String.format("ansible-playbook %s -i %s --limit %s", playbook1.getAbsolutePath(), inventory1.getAbsolutePath(), ListOfTargets);

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

