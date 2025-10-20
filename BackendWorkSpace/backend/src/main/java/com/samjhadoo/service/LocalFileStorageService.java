package com.samjhadoo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.file-storage.location:uploads}")
    private String rootLocation;

    private Path rootPath;

    @PostConstruct
    public void init() {
        try {
            this.rootPath = Paths.get(rootLocation).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
            log.info("File storage initialized at: {}", rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String storeFile(String directory, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }

            // Create directory if it doesn't exist
            Path targetDirectory = rootPath.resolve(directory);
            createDirectoryIfNotExists(targetDirectory.toString());

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = generateUniqueFilename(originalFilename);
            Path targetFile = targetDirectory.resolve(filename);

            // Save the file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path
            return Paths.get(directory).resolve(filename).toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public Path loadFileAsResource(String filePath) {
        try {
            Path file = rootPath.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return file;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path file = rootPath.resolve(filePath).normalize();
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public Stream<Path> listFiles(String directory) {
        try {
            Path dir = rootPath.resolve(directory);
            return Files.walk(dir, 1)
                    .filter(path -> !path.equals(dir))
                    .map(dir::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to list files", e);
        }
    }

    @Override
    public String getContentType(String filePath) {
        try {
            return Files.probeContentType(loadFileAsResource(filePath));
        } catch (IOException e) {
            log.warn("Could not determine content type for: {}", filePath, e);
            return "application/octet-stream";
        }
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            return Files.size(loadFileAsResource(filePath));
        } catch (IOException e) {
            throw new StorageException("Could not determine file size: " + filePath, e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            Path file = rootPath.resolve(filePath).normalize();
            return Files.exists(file) && !Files.isDirectory(file);
        } catch (InvalidPathException e) {
            return false;
        }
    }

    @Override
    public boolean createDirectoryIfNotExists(String directoryPath) {
        try {
            Path dir = rootPath.resolve(directoryPath).normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.debug("Created directory: {}", dir);
                return true;
            }
            return true;
        } catch (IOException e) {
            log.error("Could not create directory: {}", directoryPath, e);
            return false;
        }
    }

    @Override
    public String getPublicUrl(String filePath) {
        // In a production environment, this would return a CDN URL or similar
        return "/api/files/" + filePath;
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath) throws IOException {
        Path source = rootPath.resolve(sourcePath).normalize();
        Path target = rootPath.resolve(targetPath).normalize();
        
        // Create parent directories if they don't exist
        createDirectoryIfNotExists(target.getParent().toString());
        
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath) throws IOException {
        Path source = rootPath.resolve(sourcePath).normalize();
        Path target = rootPath.resolve(targetPath).normalize();
        
        // Create parent directories if they don't exist
        createDirectoryIfNotExists(target.getParent().toString());
        
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootPath.toFile());
    }
}

class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
