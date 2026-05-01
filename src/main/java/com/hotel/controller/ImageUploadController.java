package com.hotel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGE_COUNT = 5;
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final float COMPRESSION_QUALITY = 0.75f;

    @Value("${app.upload.dir:uploads/reviews}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
    }

    @PostMapping("/upload/review-images")
    public ResponseEntity<Map<String, Object>> uploadReviewImages(
            @RequestParam("files") MultipartFile[] files) {

        Map<String, Object> response = new HashMap<>();
        List<String> urls = new ArrayList<>();

        if (files == null || files.length == 0) {
            response.put("success", false);
            response.put("message", "没有选择文件");
            return ResponseEntity.badRequest().body(response);
        }

        if (files.length > MAX_IMAGE_COUNT) {
            response.put("success", false);
            response.put("message", "最多上传" + MAX_IMAGE_COUNT + "张图片");
            return ResponseEntity.badRequest().body(response);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String extension = getExtension(originalName);
            if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                response.put("success", false);
                response.put("message", "不支持的图片格式: " + extension + "，仅支持 " + String.join(", ", ALLOWED_EXTENSIONS));
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("message", "图片大小超过限制(5MB): " + originalName);
                return ResponseEntity.badRequest().body(response);
            }

            try {
                String fileName = UUID.randomUUID().toString() + "." + extension.toLowerCase();
                Path targetPath = uploadPath.resolve(fileName);

                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    response.put("success", false);
                    response.put("message", "无法解析图片: " + originalName);
                    return ResponseEntity.badRequest().body(response);
                }

                BufferedImage resized = resizeImage(image);
                writeCompressedImage(resized, extension.toLowerCase(), targetPath.toFile());

                urls.add("/uploads/reviews/" + fileName);
            } catch (IOException e) {
                response.put("success", false);
                response.put("message", "图片上传失败: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        response.put("success", true);
        response.put("urls", urls);
        response.put("message", "上传成功");
        return ResponseEntity.ok(response);
    }

    private BufferedImage resizeImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return original;
        }

        double ratio = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }

    private void writeCompressedImage(BufferedImage image, String extension, File output) throws IOException {
        if ("png".equals(extension)) {
            ImageIO.write(image, "png", output);
            return;
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(
                "webp".equals(extension) ? "webp" : "jpeg");
        if (!writers.hasNext()) {
            ImageIO.write(image, extension, output);
            return;
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(output))) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(COMPRESSION_QUALITY);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : null;
    }
}
