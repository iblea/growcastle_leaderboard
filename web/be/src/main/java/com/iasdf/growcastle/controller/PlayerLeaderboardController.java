package com.iasdf.growcastle.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.iasdf.growcastle.common.ArgChecker;
import com.iasdf.growcastle.dto.ErrorReturn;

import org.springframework.http.HttpStatus;

@Controller
public class PlayerLeaderboardController {

    @GetMapping("/player/leaderboard")
    public ResponseEntity<Object> getMethodName(
        @RequestParam(name = "name", required = false, defaultValue = "") String name,
        @RequestParam(name = "cnt", required = false, defaultValue = "0") Integer cnt
    ) {
        ArgChecker.isValidUserName(name);
        int showCnt = cnt;
        ArgChecker.isValidCnt(showCnt);

        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", null);
        return ResponseEntity.ok("test");
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(getErrorJson("Argument Error"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", 0);
        response.put("msg", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    private void isValidName(String name) {
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name is too short");
        }
        if (name.length() > 21) {
            throw new IllegalArgumentException("Name is too long");
        }
        Matcher matcher = this.namePattern.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Name is invalid, Only alphabet, number, space, '_', '-' are allowed");
        }
    }

    public Map<String, Object> getErrorJson(String errmsg) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", 0);
        response.put("msg", errmsg);
        return response;
    }
}
