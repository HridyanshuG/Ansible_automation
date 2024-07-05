package com.nexthink.intern.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController("/ansible")
public class AnsibleRestService {
    Logger logger = LoggerFactory.getLogger(AnsibleRestService.class);

    @Autowired
    private AnsibleExecutor ansibleExecutor;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private FileService fileService;

    //Creating a new thread to asynchronously execute the given task by the user using REST service
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    //provides list of playbooks in path specified as per the environment variable playbookPath
    //provides list of inventory files in path specified as per the environment variable playbookPath

    //main ansible execution class takes inventory file and playbook as parameter and runs it for all the servers in the playbook
    @PostMapping("/execute")
    public String runPlaybook(@RequestBody PlaybookRequest request) {
        logger.info("Starting execution of playbook successfully received request");
        Job job = new Job();
        job.setPlaybook(request.getPlaybook());
        job.setTarget(request.getTarget());
        job.setStatus("ongoing");
        Job savedJob = jobRepository.save(job);
        executorService.submit(() -> {
            try {
                ExecuteResult result = ansibleExecutor.execute(savedJob.getPlaybook(), savedJob.getTarget());

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
        return savedJob.getId() + savedJob.getStatus();
    }
    //make file to donwload link available to the user using given file path
    @GetMapping("/jmc/{id}")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable Long id) throws IOException {
        String filePath = "/Users/hridyanshu.ghura/Desktop/JMC_result/172.16.47.216_jmc.jfr";
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(new FileSystemResource(file));
    }



}

