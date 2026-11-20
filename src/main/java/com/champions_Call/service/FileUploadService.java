package com.champions_Call.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class FileUploadService {

    // Define where to save files (e.g., inside src/main/resources/static/uploads)
    private final String UPLOAD_DIR = "uploads/";

    public String save(MultipartFile file) {
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            
            // Save the file
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR + filename));
            
            return "/uploads/" + filename; // This is the URL returned to your DTO
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }
} 
