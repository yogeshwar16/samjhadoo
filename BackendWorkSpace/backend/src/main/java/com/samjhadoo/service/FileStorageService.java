package com.samjhadoo.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {
    /**
     * Store a file in the specified directory
     * @param directory Directory path (e.g., "profiles/123")
     * @param file File to store
     * @return URL or path to the stored file
     */
    String storeFile(String directory, MultipartFile file);
    
    /**
     * Load a file as a resource
     * @param filePath Path to the file
     * @return The file as a resource
     */
    Path loadFileAsResource(String filePath);
    
    /**
     * Delete a file
     * @param filePath Path to the file to delete
     * @return true if deletion was successful
     */
    boolean deleteFile(String filePath);
    
    /**
     * Get all files in a directory
     * @param directory Directory to list files from
     * @return Stream of file paths
     */
    Stream<Path> listFiles(String directory);
    
    /**
     * Get the content type of a file
     * @param filePath Path to the file
     * @return Content type as string
     */
    String getContentType(String filePath);
    
    /**
     * Get file size in bytes
     * @param filePath Path to the file
     * @return File size in bytes
     */
    long getFileSize(String filePath);
    
    /**
     * Check if a file exists
     * @param filePath Path to check
     * @return true if file exists
     */
    boolean fileExists(String filePath);
    
    /**
     * Create a directory if it doesn't exist
     * @param directoryPath Path to the directory
     * @return true if directory was created or already exists
     */
    boolean createDirectoryIfNotExists(String directoryPath);
    
    /**
     * Get the public URL for a stored file
     * @param filePath Path to the file
     * @return Publicly accessible URL
     */
    String getPublicUrl(String filePath);
    
    /**
     * Copy a file to a new location
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @return true if copy was successful
     */
    boolean copyFile(String sourcePath, String targetPath) throws IOException;
    
    /**
     * Move a file to a new location
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @return true if move was successful
     */
    boolean moveFile(String sourcePath, String targetPath) throws IOException;
    
    /**
     * Generate a unique filename with original extension
     * @param originalFilename Original filename
     * @return Generated unique filename
     */
    String generateUniqueFilename(String originalFilename);
}
