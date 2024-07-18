package com.nexthink.intern.automation;

import com.nexthink.intern.automation.executor.AnsibleExecutorService;
import com.nexthink.intern.automation.util.AnsibleEnv;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@RestController
@RequestMapping("api")
public class AnsibleRestService {

    Logger logger = LoggerFactory.getLogger(AnsibleRestService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private FileService fileService;
    @Autowired
    private static AnsibleEnv ansibleEnv;


    @GetMapping("/targets/{inventoryfile}")
    public List<String> getTargets(@PathVariable String inventoryfile) throws IOException {
        return AnsibleInventoryReader.getHostsFromInventory(inventoryfile);
    }
    //make file to donwload link available to the user using given file path
    @GetMapping("/jmc/{id}/{servername}")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable Long id,@PathVariable String servername) throws IOException {
        //ResponseEntity<FileSystemResource>
        String playbookPath = ansibleEnv.getRootPath();
        String inventoryFilePath = playbookPath + "/inventory/" + "inventory.ini";
        File inventoryFileActual = new File(inventoryFilePath);
        List<String> hosts = new ArrayList<>();
        Ini ini = new Ini(inventoryFileActual);
        Map<String, String> serverNamesAndIPs = new HashMap<>();
        // Get the "servers" section
        Profile.Section serversSection = ini.get("servers");

        if (serversSection != null) {
            for (Map.Entry<String, String> entry : serversSection.entrySet()) {
                String serverName = entry.getKey().split("\\s+", 2)[0]; // Splitting serverName at the first space and taking the first part
                String serverInfo = entry.getValue();
                // Adjusting the regex to capture up to the first space after ansible_host=
                String ipAddress = serverInfo.split("\\s+", 2)[0];
                serverNamesAndIPs.put(serverName, ipAddress);
            }
        }

        String FilePath = AnsibleEnv.getRootPath() + "/Result/" + id + "/"+ serverNamesAndIPs.get(servername) + "/Result_jmc.jfr";

        File file = new File(FilePath);
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

    @GetMapping("/jobstatus/{id}")
    public Job getJobStatus(@PathVariable Long id) {
        return jobRepository.findById(id).get();
    }
    @GetMapping("/viewOutput/{id}")
    public ResponseEntity<?> viewOutput(@PathVariable Long id) {
        String filePath = ansibleEnv.getRootPath() + "/Result/" + id + "/output.txt";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            Map<String, String> response = new HashMap<>();
            response.put("content", content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error reading file: " + filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file: " + e.getMessage());
        }
    }
    @GetMapping("/jobResult/{id}")
    public ResponseEntity<List<Artifacts>> getJob(@PathVariable Long id) {
        JobResult jobResult = new JobResult();
        Job currentjob = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        jobResult.setId(id);
        jobResult.setStatus(currentjob.getStatus());
        List<Artifacts> artifacts =  buildArtifacts(currentjob);
        jobResult.setArtifacts(artifacts);
        //model.addAttribute("globalists", artifacts);
        //model.addAttribute("job", currentjob);
        String serverNames = currentjob.getListOfTargets(); // Assuming getServerNames() returns the comma-separated string
        List<String> serverList = Arrays.asList(serverNames.split(","));
        //model.addAttribute("servers", serverList);
        return ResponseEntity.ok(artifacts);
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
    @GetMapping("/ListOfTargets/{id}")
    public List<String> getTargets(@PathVariable Long id) {
        Job job = jobRepository.findById(id).get();
        return Arrays.asList(job.getListOfTargets().split(","));
    }
}

