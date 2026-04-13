package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.security.SecurityUser;
import com.canteen.service.AiRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiRecommendController {

    private final AiRecommendService aiRecommendService;

    /**
     * 非流式推荐（兼容旧接口）
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody Map<String, Object> body,
                                                  @AuthenticationPrincipal SecurityUser securityUser) {
        String query = (String) body.getOrDefault("query", "");
        List<Map<String, Object>> cart = (List<Map<String, Object>>) body.get("cart");
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");
        Long userId = securityUser != null ? securityUser.getId() : null;
        Map<String, Object> result = aiRecommendService.recommend(query, cart, history, userId);
        return Result.ok(result);
    }

    /**
     * 流式推荐（SSE）
     * 事件类型：
     *   - {type:"token", token:"文字片段"}  逐字输出
     *   - {type:"result", content:"完整文案", dishes:[...], action:"跳转目标"}  最终结果
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRecommend(@RequestBody Map<String, Object> body,
                                       @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            String query = (String) body.getOrDefault("query", "");
            List<Map<String, Object>> cart = (List<Map<String, Object>>) body.get("cart");
            List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");
            Long userId = securityUser != null ? securityUser.getId() : null;
            return aiRecommendService.streamRecommend(query, cart, history, userId);
        } catch (Exception e) {
            log.error("SSE endpoint error", e);
            SseEmitter emitter = new SseEmitter(5_000L);
            try {
                emitter.send(SseEmitter.event().data(
                        Map.of("type", "result",
                               "content", "抱歉，服务暂时不可用，请稍后再试。",
                               "dishes", List.of()),
                        MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }
    }
}
