package com.iasdf.growcastle.controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.HttpStatus;

@Controller
public class PlayerLeaderboardController {

    @GetMapping("/player/leaderboard")
    public ResponseEntity<Object> getMethodName(
        @RequestParam(name = "name", required = false, defaultValue = "") String name,
        @RequestParam(name = "cnt", required = false, defaultValue = "100") int cnt
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", null);
        return ResponseEntity.ok("test");
    }

}
