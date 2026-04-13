package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.service.AppwriteStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final AppwriteStorageService appwriteStorageService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 管理端图片上传（菜品图片等）
     */
    @PostMapping("/api/admin/upload/image")
    public Result<Map<String, String>> adminUploadImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    /**
     * 移动端图片上传（头像等）
     */
    @PostMapping("/api/mobile/upload/image")
    public Result<Map<String, String>> mobileUploadImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    private Result<Map<String, String>> doUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail("文件大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        boolean allowed = false;
        if (contentType != null) {
            for (String type : ALLOWED_TYPES) {
                if (type.equals(contentType)) {
                    allowed = true;
                    break;
                }
            }
        }
        if (!allowed) {
            return Result.fail("仅支持 JPG/PNG/GIF/WebP 格式的图片");
        }

        try {
            String imageUrl = appwriteStorageService.uploadFile(file);
            return Result.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            return Result.fail("图片上传失败: " + e.getMessage());
        }
    }
}
