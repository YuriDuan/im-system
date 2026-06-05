package com.im;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传服务
 */
@Service
public class FileUploadService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 图片
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
            "zip", "rar", "7z", // 压缩包
            "mp3", "mp4", "avi", "mov", "wmv" // 多媒体
    );

    /**
     * 上传文件
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            result.put("success", false);
            result.put("message", "文件大小不能超过10MB");
            return result;
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            result.put("success", false);
            result.put("message", "无效的文件名");
            return result;
        }

        // 验证文件扩展名
        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            result.put("success", false);
            result.put("message", "不支持的文件类型: " + fileExtension);
            return result;
        }

        try {
            // 创建上传目录
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 生成唯一的文件名
            String uniqueFileName = generateUniqueFileName(originalFileName);
            Path filePath = Paths.get(uploadDir, uniqueFileName);

            // 保存文件
            Files.write(filePath, file.getBytes());

            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("fileName", originalFileName);
            result.put("savedFileName", uniqueFileName);
            result.put("fileUrl", "/api/files/download/" + uniqueFileName);
            result.put("fileSize", file.getSize());
            result.put("fileType", getFileType(fileExtension));
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 下载文件
     */
    public File downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            File file = filePath.toFile();

            if (!file.exists()) {
                return null;
            }

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 生成存储文件名 — 保留原始文件名，仅在同名文件已存在时追加序号
     */
    private String generateUniqueFileName(String originalFileName) {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        String baseName = originalFileName;
        String extension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = originalFileName.substring(0, lastDot);
            extension = originalFileName.substring(lastDot); // 含 "." 例如 ".jpg"
        }

        String candidate = originalFileName;
        int counter = 1;
        while (new File(uploadDirectory, candidate).exists()) {
            candidate = baseName + "(" + counter + ")" + extension;
            counter++;
        }
        return candidate;
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String extension) {
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension)) {
            return "image";
        } else if (Arrays.asList("mp3", "mp4", "avi", "mov", "wmv").contains(extension)) {
            return "media";
        } else {
            return "file";
        }
    }
}
