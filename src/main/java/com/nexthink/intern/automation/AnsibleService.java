package com.nexthink.intern.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("/ansible")
public class AnsibleService {

    @Autowired
    AnsibleExecutor ansibleExecutor;

    @GetMapping("/playbook/{id}/{target}")
    public ExecuteResult runPlaybook(@PathVariable("id") String playbook, @PathVariable("target") String target){
        try {
            return ansibleExecutor.execute(playbook, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
