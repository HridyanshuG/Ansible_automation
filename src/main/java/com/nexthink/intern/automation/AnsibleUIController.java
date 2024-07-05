package com.nexthink.intern.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AnsibleUIController {
    @Autowired
    private FileService fileService;
    @Autowired
    private JobRepository jobRepository;
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }
    @GetMapping("/playbooks")
    public String getPlaybooks(Model model) {
        String folderPath = System.getenv("playbookPath");
        List<String> listPlaybooks =  fileService.getFileNames(folderPath+ "/playbook");
        model.addAttribute("playbooks", listPlaybooks);
        //String folderPath2 = System.getenv("playbookPath");
        List<String> InventoryList= fileService.getFileNames(folderPath + "/inventory");
        model.addAttribute("inventory", InventoryList);
        return "execute_playbook";
    }

    @GetMapping("/targets/{inventoryfile}")
    public List<String> getTargets(@PathVariable String inventoryfile){
        return AnsibleInventoryReader.getHostsFromInventory(inventoryfile);
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
