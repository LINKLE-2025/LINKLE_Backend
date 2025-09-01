package com.linkle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    
    @GetMapping("/test")
    public String test() {
        return "테스트";
    }
    
}
