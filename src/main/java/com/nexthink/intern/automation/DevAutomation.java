package com.nexthink.intern.automation;

import com.nexthink.intern.automation.util.AnsibleEnv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

@SpringBootApplication
public class DevAutomation implements CommandLineRunner {

	private static Logger logger = Logger.getLogger(DevAutomation.class.getName());
	@Autowired
	private AnsibleEnv ansibleEnv;

    @Autowired
    private AnsibleRestService ansible;

	public static void main(String[] args) {
		SpringApplication.run(DevAutomation.class, args);
	}

	@Override
	public void run(String args[]) throws Exception {
		setupValidation();
	}

	public void setupValidation(){
		String folderPath = System.getenv(AnsibleConstants.PLAYBOOK_PATH);
		logger.info("folderPath = " + folderPath);
		ansibleEnv.setRootPath(folderPath);
		if(folderPath == null){
			throw new RuntimeException(AnsibleConstants.PLAYBOOK_PATH+" environment is not setup");
		}
		if( !Files.exists(Paths.get(folderPath))) {
			throw new RuntimeException(folderPath +" is not valid");
		}
		//TODO handle other path and file validation
	}





}
