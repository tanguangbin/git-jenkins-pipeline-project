package org.tangb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
    @Value("tangb")
    private String message;

    @RequestMapping("/")

    public String sayHi(){
        return "hello" + message;
    }
}
