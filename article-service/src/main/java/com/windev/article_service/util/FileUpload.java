package com.windev.article_service.util;

import com.windev.article_service.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUpload {
    @Value("${app.upload.dir}")
    private static String uploadDir;
    public static String saveImage(MultipartFile file) {
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
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            log.info("saveImage() --> Image saved to: {}", filePath.toString());

            return fileName;
        } catch (IOException e) {
            log.error("saveImage() --> Failed to save image", e);
            throw new GlobalException("Failed to save image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static boolean isImage(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
                contentType.equalsIgnoreCase("image/png") ||
                contentType.equalsIgnoreCase("image/jpg") ||
                contentType.equalsIgnoreCase("image/gif");
    }

    public static String createThumbnail(String mainImagePath) {
        try {
            String projectDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectDir, uploadDir);
            String thumbnailName = "thumb_" + mainImagePath;
            Path thumbnailPath = uploadPath.resolve(thumbnailName);

            Thumbnails.of(uploadPath.resolve(mainImagePath).toFile())
                    .size(150, 150)
                    .toFile(thumbnailPath.toFile());

            log.info("createThumbnail() --> Thumbnail created at: {}", thumbnailPath.toString());

            return thumbnailName;
        } catch (IOException e) {
            log.error("createThumbnail() --> Failed to create thumbnail", e);
            throw new GlobalException("Failed to create thumbnail", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
