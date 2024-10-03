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

    // @Autowired
    private final PlayerService playerService;

    public PlayerLeaderboardController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/player/leaderboard")
    public ResponseEntity<Object> leaderboards(
        @RequestParam(name = "cnt", required = false, defaultValue = "0") Integer cnt
    ) {
        ArgChecker.isValidUserName(name);
        int showCnt = cnt;
        ArgChecker.isValidCnt(showCnt);

        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", players);

        return ResponseEntity.ok("test");
    }
        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", null);
        return ResponseEntity.ok("test");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorReturn> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            // .body(new ErrorReturn(ex.getMessage()));
            .body(new ErrorReturn(paramName + " is invalid"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorReturn> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorReturn(ex.getMessage()));
    }

}
