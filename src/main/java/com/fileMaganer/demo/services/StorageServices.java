package com.fileMaganer.demo.services;

import com.fileMaganer.demo.exemptions.FileGenericException;
import com.fileMaganer.demo.exemptions.FileStorageException;
import com.fileMaganer.demo.model.FileInfoRequest;
import com.fileMaganer.demo.model.FileUrlRequest;
import com.fileMaganer.demo.repositories.StorageRepositories;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource; //
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StorageServices implements StorageRepositories {

    // Inyecta desde el archivo application.properties la ruta base donde se almacenarán los archivos.
    @Value("${media.location}")
    private String mediaLocation;

    // Representa el path raíz en el sistema de archivos donde se guardarán los archivos y carpetas.
    private Path rootLocation;

    @Override
    @PostConstruct
    public void init()  {
        // Inicializa el path raíz a partir de la ruta configurada y asegura que exista creando los directorios necesarios.
        rootLocation = Paths.get(mediaLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Error al inicializar el directorio");
        }
    }

    @Override
    public List<FileUrlRequest> storeFiles(MultipartFile[] files, String dir) {
        if (files == null || files.length == 0){
            throw new FileGenericException("No se recibieron archivos.");
        }

       List<FileUrlRequest> responses = new ArrayList<>();

        for (MultipartFile file : files){
           responses.add(store(file, dir));
        }
        return responses;
    }

    /*Metodo para subir un archivo al servidor, guardándolo en una subcarpeta específica.
     Devuelve un objeto con la URL para acceder al archivo una vez almacenado.*/
    @Override
    public FileUrlRequest store(MultipartFile file, String dir) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("No se puede almacenar un archivo vacío.");
            }

            String filename = file.getOriginalFilename();
            assert filename != null;

            Path directory = rootLocation.resolve(dir).normalize();
            Files.createDirectories(directory);

            Path destinationFile = directory.resolve(Paths.get(filename).normalize());

            if (!destinationFile.normalize().startsWith(rootLocation.normalize())) {
                throw new FileGenericException("No se permite guardar fuera del directorio base.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            String encodedDir = URLEncoder.encode(dir, StandardCharsets.UTF_8);
            String url = "http://localhost:8080/media/download-file?filename=" + encodedFilename + "&directory=" + encodedDir;

            return new FileUrlRequest(filename, encodedDir, url);
        } catch (IOException e) {
            throw new FileStorageException("Error al guardar el archivo.", e);
        }
    }


    // Metodo que permite recuperar un archivo almacenado, como un recurso (Resource), para su descarga o visualización.
    @Override
    public Resource loadsAsResource(String filename, String dir) throws FileNotFoundException {
        try {
            Path file = rootLocation.resolve(dir).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("No se puede leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("URL del archivo no definida: " + filename, e);
        }
    }

    /*Metodo que lista el contenido de un directorio específico.
     Devuelve una lista de archivos y subcarpetas inmediatas con sus respectivas URLs.*/
    @Override
    public List<FileInfoRequest> loadAllFilenames(String dir) throws FileNotFoundException {
        Path directory = rootLocation.resolve(dir).normalize();

        if (!Files.exists(directory)){
            throw new FileNotFoundException("El directorio no existe:" + dir);
        }
        try(Stream<Path> stream = Files.list(directory)){
           return stream.map(path -> {
               String relativePath = rootLocation.relativize(path).toString().replace("\\", "/");

               if (Files.isDirectory(path)){
                   String encodedDir = URLEncoder.encode(relativePath, StandardCharsets.UTF_8);
                   String url = "http://localhost:8080/media/list-directory?path=" + encodedDir;
                   return new FileInfoRequest(relativePath, url);

               }else {

                   String fileName = path.getFileName().toString();
                   String parentDir = rootLocation.relativize(path.getParent()).toString().replace("\\", "/");

                   String encodedFile = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                   String encodedDir = URLEncoder.encode(parentDir, StandardCharsets.UTF_8);
                   String url = "http://localhost:8080/media/download-file?filename=" + encodedFile + "&directory=" + encodedDir;

                   return new FileInfoRequest(relativePath, url);
               }
           }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileNotFoundException("No se puede listar el contenido del directorio");
        }
    }

    // Metodo que crea una nueva carpeta dentro de un directorio padre especificado.
    @Override
    public void createDirectory(String folderName, String parentDir) {

        try {
            Path directory = rootLocation.resolve(parentDir).resolve(folderName).normalize();
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new FileGenericException("Error el crear al directorio");
        }

    }

    @Override
    public void deleteFile(String fileName, String dir){

        Path targetPath;

        try {

            if (fileName != null && !fileName.isBlank()){

                if (dir != null && !dir.isBlank()){
                    targetPath = rootLocation.resolve(dir).resolve(fileName).normalize();
                }else {
                    targetPath = rootLocation.resolve(fileName).normalize();
                }

                if (Files.exists(targetPath) && Files.isRegularFile(targetPath)){
                    Files.delete(targetPath);
                }else {
                    throw new FileNotFoundException("Archivo no encontrado : " + fileName);
                }

            } else if (dir != null && !dir.isBlank()) {

                targetPath = rootLocation.resolve(dir).normalize();

                if (Files.exists(targetPath) && Files.isDirectory(targetPath)){
                    deleteRecursively(targetPath);
                }else {
                    throw new FileNotFoundException("Directorio no encontrado : " + fileName);
                }

            } else {
                throw new FileStorageException("Debe proporcionar al menos el nombre del archivo o la ruta del directorio.");
            }
        } catch (IOException e) {
            throw new FileStorageException("Error al eliminar directorio o archivo");
        }

    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)){
            try(Stream<Path> entries = Files.list(path)){
                for (Path entry : entries.toList()){
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }
}
