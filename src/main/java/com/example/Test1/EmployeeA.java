package com.example.Test1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
public class EmployeeA {
    @Autowired
    Testing helloWorld;

    @GetMapping("/{empId}")
    public String getEmployee(Integer empId){
        helloWorld.display();
        return "Hello world " + helloWorld.display();
    }
}
