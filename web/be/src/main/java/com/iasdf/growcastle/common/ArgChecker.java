package com.iasdf.growcastle.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgChecker {

    private static Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 _-]+$");

    public ArgChecker() { }

    public static void isValidUserName(String name) {
        // name argument 빈 값 처리
        if (name.length() == 0) {
            // throw new IllegalArgumentException("Name is too short");
            return ;
        }
        if (name.length() > 21) {
            throw new IllegalArgumentException("Name is too long");
        }
        Matcher matcher = USERNAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Name is invalid, Only alphabet, number, space, '_', '-' are allowed");
        }
    }

}