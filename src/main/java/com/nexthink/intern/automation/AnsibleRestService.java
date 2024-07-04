package com.nexthink.intern.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/playbooks")
    public List<String> getPlaybooks() {
        String folderPath = System.getenv("playbookPath");
        folderPath = folderPath + "/playbook";
        return fileService.getFileNames(folderPath);
    }
    //provides list of inventory files in path specified as per the environment variable playbookPath
    @GetMapping("/inventory")
    public List<String> getInventory() {
        String folderPath = System.getenv("playbookPath");
        return fileService.getFileNames(folderPath + "/inventory");
    }
    //provides list of targets by their name as specified in the chosen inventory file


    @GetMapping("/targets/{inventoryfile}")
    public List<String> getTargets(@PathVariable String inventoryfile){

        return AnsibleInventoryReader.getHostsFromInventory(inventoryfile);
    }
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


    @GetMapping("/jobs/{id}")
    public JobResult getJob(@PathVariable Long id) {
        JobResult jobResult = new JobResult();
        Job currentjob = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        jobResult.setId(id);
        jobResult.setStatus(currentjob.getStatus());
        List<Artifacts> artifacts =  buildArtifacts(currentjob);
        jobResult.setArtifacts(artifacts);
        return jobResult;
    }

    private List<Artifacts> buildArtifacts(Job job) {
        List<Artifacts> artifacts = new ArrayList<>();
        if(job.getPlaybook().equals("jmc.yml")){
            Artifacts artifacts1 = new Artifacts();
            artifacts1.setType("url");
            artifacts1.setValue("http://localhost:8080/jmc"+job.getId()+"jmc.jfr");
            artifacts.add(artifacts1);
        }
        return artifacts;
    }
}

