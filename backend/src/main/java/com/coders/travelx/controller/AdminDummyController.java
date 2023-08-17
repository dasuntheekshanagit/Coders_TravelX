package com.coders.travelx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
public class AdminDummyController {
    @GetMapping
    public String testAdminToken(){
        return "Welcome Admin. You have used a valid token";

    }
}
