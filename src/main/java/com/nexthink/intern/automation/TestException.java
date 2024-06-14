package com.nexthink.intern.automation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestException {

    public static void main(String[] args){
        File file = new File("test");
        try {
            Scanner reader = new Scanner(file);
            //write db
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
