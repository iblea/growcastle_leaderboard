package com.iasdf.growcastle.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MainController {

    // @GetMapping("/")
    // public String home() {
    //     // src/main/resources/templates/home.html
    //     return "home";
    // }

    // @GetMapping("/")
    // public void mainpage(HttpServletResponse res) {
    //     try {
    //         PrintWriter out = res.getWriter();
    //         out.println("Hello, Spring!");
    //         out.close();
    //     } catch (IOException ex) {
    //         ex.printStackTrace();
    //     }
    // }

    @GetMapping("/")
    public ResponseEntity<Object> mainpage(HttpServletResponse res) {
        return ResponseEntity.ok("Hello Spring!");
    }

}
