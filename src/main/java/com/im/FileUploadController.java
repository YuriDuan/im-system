package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {
    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileUploadService.uploadFile(file));
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            Map<String, Object> uploadResult = fileUploadService.uploadFile(file);
            if (Boolean.TRUE.equals(uploadResult.get("success"))) {
                successCount++;
            } else {
                failCount++;
            }
        }

        result.put("success", failCount == 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", "成功上传 " + successCount + " 个文件，失败 " + failCount + " 个");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        File file = fileUploadService.downloadFile(fileName);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        // 根据扩展名判断 MIME 类型
        String contentType = determineContentType(fileName);
        // 图片类文件用 inline 让浏览器直接显示，其他文件用 attachment 触发下载
        boolean isImage = contentType != null && contentType.startsWith("image/");
        String disposition = isImage ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    private String determineContentType(String fileName) {
        String name = fileName.toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".bmp")) return "image/bmp";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "application/msword";
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (name.endsWith(".zip")) return "application/zip";
        if (name.endsWith(".mp3")) return "audio/mpeg";
        if (name.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        boolean deleted = fileUploadService.deleteFile(fileName);
        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        result.put("message", deleted ? "文件删除成功" : "文件删除失败");
        return ResponseEntity.ok(result);
    }
}
