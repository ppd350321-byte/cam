package com.canteen.controller;

import com.canteen.service.AppwriteStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class FileProxyController {

    private final AppwriteStorageService appwriteStorageService;

    /**
     * 代理访问 Appwrite 存储的文件（无需认证，用于图片展示）
     */
    @GetMapping("/api/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileId) {
        if (!fileId.matches("^[a-zA-Z0-9]{1,36}$")) {
            return ResponseEntity.badRequest().build();
        }
        ResponseEntity<byte[]> appwriteResponse = appwriteStorageService.getFile(fileId);
        if (!appwriteResponse.getStatusCode().is2xxSuccessful() || appwriteResponse.getBody() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .contentType(appwriteResponse.getHeaders().getContentType())
                .body(appwriteResponse.getBody());
    }
}
