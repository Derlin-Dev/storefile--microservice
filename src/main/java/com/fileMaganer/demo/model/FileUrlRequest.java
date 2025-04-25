package com.fileMaganer.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileUrlRequest {

    private String fileName;
    private String directory;
    private String url;

}
