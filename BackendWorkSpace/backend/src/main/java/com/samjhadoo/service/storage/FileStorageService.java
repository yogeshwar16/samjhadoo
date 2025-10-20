package com.samjhadoo.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for file storage operations.
 */
public interface FileStorageService {
    
    /**
     * Stores a file
     * @param file File to store
     * @param directory Directory to store in
     * @return URL of stored file
     */
    String storeFile(MultipartFile file, String directory);
    
    /**
     * Generates a thumbnail for an image
     * @param fileUrl Original file URL
     * @return Thumbnail URL
     */
    String generateThumbnail(String fileUrl);
    
    /**
     * Deletes a file
     * @param fileUrl File URL to delete
     */
    void deleteFile(String fileUrl);
    
    /**
     * Gets file download URL
     * @param fileUrl File URL
     * @return Download URL
     */
    String getDownloadUrl(String fileUrl);
}
