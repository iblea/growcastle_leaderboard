package com.iasdf.growcastle.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    // @GetMapping("/")
    // public String home() {
    //     return "home";
    // }

    @GetMapping("/")
    public void foo(HttpServletResponse res) {
        try {
            PrintWriter out = res.getWriter();
            out.println("Hello, Spring!");
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public String hometest() {
        return "Response!";
    }

}
