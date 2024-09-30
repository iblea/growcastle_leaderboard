package com.iasdf.growcastle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorReturn {

    boolean success;
    String msg;

    public ErrorReturn() {
        success = false;
        msg = "";
    }

    public ErrorReturn(String msg) {
        success = false;
        this.msg = msg;
    }

    public ErrorReturn(boolean success) {
        this.success = success;
        this.msg = "";
    }

    public ErrorReturn(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }
}
