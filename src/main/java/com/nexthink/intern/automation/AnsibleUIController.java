package com.nexthink.intern.automation;

import com.nexthink.intern.automation.executor.AnsibleExecutor;
import com.nexthink.intern.automation.util.AnsibleEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.nexthink.intern.automation.AnsibleInventoryReader.getHostsFromInventory;

@Controller
@RequestMapping("/ui")
public class AnsibleUIController {
    private Logger logger = LoggerFactory.getLogger(AnsibleUIController.class);
    @Autowired
    private AnsibleEnv ansibleEnv;
    @Autowired
    private AnsibleExecutor ansibleExecutor;

    @Autowired
    private FileService fileService;
    @Autowired
    private JobRepository jobRepository;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     *   provides list of playbooks in path specified as per the environment variable playbookPath
     *   provides list of inventory files in path specified as per the environment variable playbookPath
     *   main ansible execution class takes inventory file and playbook as parameter and runs it for all the servers in the playbook
     */
    @PostMapping("/execute")
    public String runPlaybook(@ModelAttribute ExecutionForm request) throws IOException {
        logger.info("Starting execution of playbook successfully received request");
        String jmcRunningTime = request.getJmcRunningTime();
        if (jmcRunningTime == null || jmcRunningTime.isEmpty()) {
            jmcRunningTime = "60"; // Default value if not provided
        }
        // Pass jmcRunningTime to the executor along with other job details
        //ansibleExecutor.submitJob(savedJob, jmcRunningTime); // Assuming the method signature is updated to accept JMCRunningTime

        Job job = new Job();
        job.setPlaybook(request.getPlaybook());
        logger.info("Starting execution of playbook successfully received playbook");
        job.setTarget("inventory.ini"); //TODO move to constant INVENTORY_FILE
        String ServerNames = String.join(",", request.getTargets());
        job.setListOfTargets(ServerNames);
        logger.info("Starting execution of playbook successfully received targets" + ServerNames);
        job.setStatus("ongoing"); //TODO move to constant  JOB_STATUS_ONGOING
        Job savedJob = jobRepository.save(job);
        logger.info("Starting execution of playbook successfully saved job");
        ansibleExecutor.submitJob(savedJob,jmcRunningTime);
        return "redirect:/ui/jobs/"+savedJob.getId();
    }

    @GetMapping("/playbooks")
    public String getPlaybooks(Model model) throws IOException {
        List<String> listPlaybooks =  fileService.getFileNames(Paths.get(AnsibleEnv.getRootPath(),"/playbook").toAbsolutePath().toString());
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
        model.addAttribute("job", currentjob);
        String serverNames = currentjob.getListOfTargets(); // Assuming getServerNames() returns the comma-separated string
        List<String> serverList = Arrays.asList(serverNames.split(","));
        model.addAttribute("servers", serverList);
        return "view_result_detail";
    }

    private List<Artifacts> buildArtifacts(Job job) {
        List<Artifacts> artifacts = new ArrayList<>();
        if(job.getPlaybook().equals("jmc.yml") && job.getStatus().equals("success")){
            Artifacts artifacts1 = new Artifacts();
            artifacts1.setType("url for jmc");
            String serverNames = job.getListOfTargets();
            List<String> serverList = Arrays.asList(serverNames.split(","));
            for (String server : serverList) {
                String url = "http://localhost:8080/api/jmc/" + job.getId() + "/" + server;
                artifacts1.addServerValue(server, url);
            }

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
