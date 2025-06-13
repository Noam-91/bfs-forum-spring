package com.bfsforum.fileservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> generateUploadUrl(
        @RequestBody Request request,
        @RequestParam(value = "expiration", defaultValue = "5", required = false) int expirationMinutes) {
        String filename = request.getFileName();
        String contentType = request.getContentType();
        try {
            String url = fileService.generatePreSignedUploadUrl(filename, contentType, expirationMinutes);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error generating upload URL: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to generate upload URL.");
        }
    }

    /**
     * Endpoint for generating a pre-signed URL for file downloads.
     *
     * @param key The S3 object key (path) of the file to be downloaded.
     * @param expirationMinutes The duration in minutes for which the URL will be valid (defaults to 10 minutes).
     * @return A ResponseEntity containing the pre-signed download URL.
     */
    @GetMapping("/download")
    public ResponseEntity<String> generateDownloadUrl(
        @RequestParam("key") String key,
        @RequestParam(value = "expiration", defaultValue = "10") int expirationMinutes) {
        try {
            String url = fileService.generatePreSignedDownloadUrl(key, expirationMinutes);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error generating download URL: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to generate download URL.");
        }
    }
}
