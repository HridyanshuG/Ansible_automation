package com.nexthink.intern.automation;

import jakarta.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController("/ansible")
public class AnsibleService {
    Logger logger = LoggerFactory.getLogger(AnsibleService.class);

    @Autowired
    private AnsibleExecutor ansibleExecutor;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private FileService fileService;


    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/playbooks")
    public List<String> getPlaybooks() {
        String folderPath = System.getenv("playbookPath");
        folderPath = folderPath + "/playbook";
        return fileService.getFileNames(folderPath);
    }

    @GetMapping("/targets")
    public List<String> getInventory() {
        String folderPath = System.getenv("playbookPath");
        return fileService.getFileNames(folderPath + "/inventory");
    }

    @PostMapping("/execute")
    public String runPlaybook(@RequestBody PlaybookRequest request) {
        logger.info("Startin execution of playbook successfully recieved request");
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
                    savedJob.setStatus("success");
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

    @GetMapping("/Testjob")
    public List<String> Testjob(){
        Job job = new Job();
        job.setPlaybook("TEST@void");
        jobRepository.save(job);
        List<String> listOfStrings = new ArrayList<>();
        listOfStrings.add("Hello");
        listOfStrings.add("World");
        return listOfStrings;
    }
    @GetMapping("/jobs/{id}")
    public Job getJob(@PathVariable Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
    }
}

