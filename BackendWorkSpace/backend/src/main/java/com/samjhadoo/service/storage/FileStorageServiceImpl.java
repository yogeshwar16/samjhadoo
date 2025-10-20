package com.samjhadoo.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Simple file storage implementation.
 * In production, use cloud storage like AWS S3, Google Cloud Storage, or Azure Blob Storage.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file-storage.base-path:./uploads}")
    private String basePath;
    
    @Value("${app.file-storage.base-url:http://localhost:8081/files}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file, String directory) {
        try {
            // Create directory if it doesn't exist
            Path directoryPath = Paths.get(basePath, directory);
            Files.createDirectories(directoryPath);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = directoryPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String fileUrl = baseUrl + "/" + directory + "/" + filename;
            log.info("Stored file: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Error storing file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public String generateThumbnail(String fileUrl) {
        // Simplified - in production, use image processing library
        // For now, just return the original URL
        log.debug("Thumbnail generation not implemented, returning original URL");
        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract path from URL
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(basePath, relativePath);
            
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", fileUrl);
            
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadUrl(String fileUrl) {
        // In production, generate signed URL for cloud storage
        return fileUrl;
    }
}
