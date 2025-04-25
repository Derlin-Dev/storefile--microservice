package com.fileMaganer.demo.controller;

import com.fileMaganer.demo.model.FileInfoRequest;
import com.fileMaganer.demo.model.FileUrlRequest;
import com.fileMaganer.demo.services.StorageServices;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


@RestController
@RequestMapping("media")
@AllArgsConstructor
public class MediaController {

    private final StorageServices services;
    private final HttpServletRequest request;

    @PostMapping("upload-file")
    public ResponseEntity<List<FileUrlRequest>> uploadFile(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam(defaultValue = "") String targetDir
    ) {
        List<FileUrlRequest> filesUrl = services.storeFiles(files, targetDir);
        return ResponseEntity.ok(filesUrl);
    }

    //Lista el contenido de un directorio
    @GetMapping("list-directory")
    public ResponseEntity<List<FileInfoRequest>> getListFile(
            @RequestParam(defaultValue = "") String path) throws IOException
    {
        List<FileInfoRequest> listFile = services.loadAllFilenames(path);
        return ResponseEntity.ok(listFile);
    }

    //Devuelve el contenido de un archivo
    @GetMapping("download-file")
    public ResponseEntity<Resource> getFile(
            @RequestParam(defaultValue = "") String directory,
            @RequestParam String filename
    ) throws IOException {
        Resource file = services.loadsAsResource(filename, directory);

        String contentType;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    //Crea un nuevo directorio
    @PostMapping("create-folder")
    public ResponseEntity<String> createDirectory(
            @RequestParam String name,
            @RequestParam(defaultValue = "") String parentPath
    ) {

        services.createDirectory(name, parentPath);
        return ResponseEntity.ok("Directorio creado.");

    }

    //Eliminar un archivo o directorio
    @DeleteMapping("delete")
    public ResponseEntity<?> deleteFile(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String path
    ) throws IOException {
        services.deleteFile(name, path);
        return ResponseEntity.ok("Archivo/directorio eliminado correctamente...");
    }
}
