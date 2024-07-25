package org.tangb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
    @Value("${tangb:Hello default}")
    private String tangb;

    @RequestMapping("/")

    public String sayHi(){
        return "hello " + tangb;
    }
}
