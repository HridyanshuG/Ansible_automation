package com.nexthink.intern.automation;

import com.nexthink.intern.automation.executor.AnsibleExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@RestController()
@RequestMapping("api")
public class AnsibleRestService {

    Logger logger = LoggerFactory.getLogger(AnsibleRestService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private FileService fileService;

    //Creating a new thread to asynchronously execute the given task by the user using REST service


    @GetMapping("/targets/{inventoryfile}")
    public List<String> getTargets(@PathVariable String inventoryfile) throws IOException {
        return AnsibleInventoryReader.getHostsFromInventory(inventoryfile);
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

    @GetMapping("/consoleoutput")
    public String consoleOutput() {
        return "123132131";
    }



}

