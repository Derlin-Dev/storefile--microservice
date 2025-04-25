package com.fileMaganer.demo.exemptions;

public class FileNotFoundException extends RuntimeException{

    public FileNotFoundException(String message){
        super(message);
    }

}
