package com.windev.article_service.util;

import com.windev.article_service.exception.GlobalException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FileUpload {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Async("taskExecutor")
    public CompletableFuture<String> saveImageAsync(String uniqueFileName, MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> saveImage(uniqueFileName, file));
    }

    @Async("taskExecutor")
    public CompletableFuture<String> createThumbnailAsync(String uniqueFileName) {
        return CompletableFuture.supplyAsync(() -> createThumbnail(uniqueFileName));
    }

    public String saveImage(String uniqueFileName, MultipartFile file) {
        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (!isImage(contentType)) {
            throw new GlobalException("Invalid image type", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra kích thước file (ví dụ: tối đa 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new GlobalException("File size exceeds limit (5MB)", HttpStatus.BAD_REQUEST);
        }

        try {
            // Giải quyết đường dẫn absolute dựa trên thư mục dự án
            String projectDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectDir, uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("saveImage() --> Created upload directory: {}", uploadPath.toString());
            }

            // Tạo tên file duy nhất
            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            log.info("saveImage() --> Image saved to: {}", filePath.toString());

            return uniqueFileName;
        } catch (IOException e) {
            log.error("saveImage() --> Failed to save image", e);
            throw new GlobalException("Failed to save image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String createThumbnail(String uniqueFileName) {
        try {
            String projectDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectDir, uploadDir);
            String thumbnailName = "thumb_" + uniqueFileName;
            Path thumbnailPath = uploadPath.resolve(thumbnailName);

            Thumbnails.of(uploadPath.resolve(uniqueFileName).toFile())
                    .size(150, 150)
                    .toFile(thumbnailPath.toFile());

            log.info("createThumbnail() --> Thumbnail created at: {}", thumbnailPath.toString());

            return thumbnailName;
        } catch (IOException e) {
            log.error("createThumbnail() --> Failed to create thumbnail", e);
            throw new GlobalException("Failed to create thumbnail", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean isImage(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
                contentType.equalsIgnoreCase("image/png") ||
                contentType.equalsIgnoreCase("image/jpg") ||
                contentType.equalsIgnoreCase("image/gif");
    }
}
