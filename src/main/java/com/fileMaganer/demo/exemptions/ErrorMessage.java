package com.fileMaganer.demo.exemptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorMessage {

    private HttpStatus httpStatus;
    private String message;

    public ErrorMessage(String messege, HttpStatus httpStatus) {
        this.message = messege;
        this.httpStatus = httpStatus;
    }


}
