package com.nexthink.intern.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevAutomation implements CommandLineRunner{

	@Autowired
	AnsibleExecutor ansibleExecutor;
    @Autowired
    private AnsibleService ansible;

	public static void main(String[] args) {
		SpringApplication.run(DevAutomation.class, args);
	}

	@Override
	public void run(String args[]) throws Exception
	{

		//ExecuteResult result = ansibleExecutor.execute("toggle_totp_false","172.16.47.216");
		//System.out.println(result.isSuccess());
	}





}
