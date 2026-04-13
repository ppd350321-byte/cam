package com.canteen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AppwriteStorageService {

    @Value("${canteen.storage.appwrite.endpoint}")
    private String endpoint;

    @Value("${canteen.storage.appwrite.project-id}")
    private String projectId;

    @Value("${canteen.storage.appwrite.api-key}")
    private String apiKey;

    @Value("${canteen.storage.appwrite.bucket-id}")
    private String bucketId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 上传文件到 Appwrite Storage，返回可直接访问的预览 URL
     */
    public String uploadFile(MultipartFile file) {
        String fileId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        String url = endpoint + "/storage/buckets/" + bucketId + "/files";
        log.info("上传文件到 Appwrite: url={}, fileId={}, fileName={}, contentType={}, size={}",
                url, fileId, file.getOriginalFilename(), file.getContentType(), file.getSize());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Appwrite-Project", projectId);
        headers.set("X-Appwrite-Key", apiKey);

        // 为文件 part 设置正确的 Content-Type
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(
                new MultipartFileResource(file), filePartHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileId", fileId);
        body.add("file", filePart);
        // 设置文件公开可读权限
        body.add("permissions[]", "read(\"any\")");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String returnedId = String.valueOf(response.getBody().get("$id"));
                String previewUrl = buildPreviewUrl(returnedId);
                log.info("文件上传成功: fileId={}, previewUrl={}", returnedId, previewUrl);
                return previewUrl;
            }
            throw new RuntimeException("Appwrite 上传失败: " + response.getStatusCode());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Appwrite API 返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("文件上传失败: Appwrite 返回 " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("文件上传失败")) {
                throw e;
            }
            log.error("文件上传到 Appwrite 失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 从 Appwrite 获取文件内容（用于代理访问）
     */
    public ResponseEntity<byte[]> getFile(String fileId) {
        String url = endpoint + "/storage/buckets/" + bucketId + "/files/" + fileId + "/view";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Appwrite-Project", projectId);
        headers.set("X-Appwrite-Key", apiKey);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
        } catch (HttpClientErrorException e) {
            log.error("获取 Appwrite 文件失败: fileId={}, status={}", fileId, e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    /**
     * 构建文件代理 URL（通过后端代理访问，避免 Appwrite 权限问题）
     */
    private String buildPreviewUrl(String fileId) {
        return "/api/files/" + fileId;
    }

    /**
     * 包装 MultipartFile 为 Spring Resource，携带原始文件名
     */
    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;

        public MultipartFileResource(MultipartFile file) {
            super(getBytes(file));
            this.filename = file.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        private static byte[] getBytes(MultipartFile file) {
            try {
                return file.getBytes();
            } catch (Exception e) {
                throw new RuntimeException("读取上传文件失败", e);
            }
        }
    }
}
