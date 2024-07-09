package com.nexthink.intern.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.nexthink.intern.automation.AnsibleInventoryReader.getHostsFromInventory;

@Controller
public class AnsibleUIController {
    Logger logger = LoggerFactory.getLogger(AnsibleUIController.class);

    @Autowired
    AnsibleEnv ansibleEnv;

    @Autowired
    private AnsibleExecutor ansibleExecutor;
    @Autowired
    private FileService fileService;
    @Autowired
    private JobRepository jobRepository;


    ExecutorService executorService = Executors.newSingleThreadExecutor();

    //provides list of playbooks in path specified as per the environment variable playbookPath
    //provides list of inventory files in path specified as per the environment variable playbookPath

    //main ansible execution class takes inventory file and playbook as parameter and runs it for all the servers in the playbook
    @PostMapping("/execute")
    public String runPlaybook(@ModelAttribute ExecutionForm request) {
        logger.info("Starting execution of playbook successfully received request");
        Job job = new Job();
        job.setPlaybook(request.getPlaybook());
        logger.info("Starting execution of playbook successfully received playbook");
        job.setTarget("inventory.ini");
        String ServerNames = String.join(",", request.getTargets());
        job.setListOfTargets(ServerNames);
        logger.info("Starting execution of playbook successfully received targets");
        job.setStatus("ongoing");
        Job savedJob = jobRepository.save(job);
        logger.info("Starting execution of playbook successfully saved job");
        executorService.submit(() -> {
            try {
                ExecuteResult result = ansibleExecutor.execute(savedJob.getPlaybook(), savedJob.getListOfTargets());

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
        return "redirect:/jobs/"+savedJob.getId();
    }
    @GetMapping("/playbooks")
    public String getPlaybooks(Model model) throws IOException {
        List<String> listPlaybooks =  fileService.getFileNames(Paths.get(ansibleEnv.getFolderPath(),"/playbook").toAbsolutePath().toString());
        model.addAttribute("playbooks", listPlaybooks);
        List<String> listInventory =  getHostsFromInventory("inventory.ini");
        model.addAttribute("inventory", listInventory);
        return "execute_playbook";
    }



    @GetMapping("/jobs/{id}")
    public String getJob(@PathVariable Long id, Model model) {
        JobResult jobResult = new JobResult();
        Job currentjob = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        jobResult.setId(id);
        jobResult.setStatus(currentjob.getStatus());
        List<Artifacts> artifacts =  buildArtifacts(currentjob);
        jobResult.setArtifacts(artifacts);
        model.addAttribute("globalists", artifacts);
        return "view_result_detail";
    }

    private List<Artifacts> buildArtifacts(Job job) {
        List<Artifacts> artifacts = new ArrayList<>();
        if(job.getPlaybook().equals("jmc.yml")){
            Artifacts artifacts1 = new Artifacts();
            artifacts1.setType("url");
            artifacts1.setValue("http://localhost:8080/jmc"+job.getId()+"jmc.jfr");
            artifacts.add(artifacts1);
        }
        if(job.getPlaybook().equals("toggle_totp_true.yml")){
            Artifacts artifacts1 = new Artifacts();
            artifacts1.setType("url");
            artifacts1.setValue("http://localhost:8080/jmc"+job.getId()+"jmc.jfr");
            artifacts.add(artifacts1);
        }
        return artifacts;
    }

    @GetMapping("/allJobs")
    public String getJob(Model model) {
        List<Job> jobs = jobRepository.findAll();
        model.addAttribute("jobs", jobs);
        return "view_result";
    }

}
