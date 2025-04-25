package com.fileMaganer.demo.repositories;

import com.fileMaganer.demo.model.FileUrlRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface StorageRepositories {

    void init() throws IOException;
    FileUrlRequest store(MultipartFile file, String dir);
    List<FileUrlRequest> storeFiles(MultipartFile[] files, String dir);
    Resource loadsAsResource(String filename, String dir) throws FileNotFoundException;
    List<?> loadAllFilenames(String dir) throws IOException;
    void createDirectory(String folderName, String parentDir) throws IOException;
    void deleteFile(String fileName, String dir);
}
