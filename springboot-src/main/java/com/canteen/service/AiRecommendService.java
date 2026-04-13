package com.canteen.service;

import com.canteen.dto.response.DishVO;
import com.canteen.entity.Dish;
import com.canteen.entity.MenuCategory;
import com.canteen.repository.DishRepository;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.VipLevelRepository;
import com.canteen.entity.VipLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiRecommendService {

    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final VipLevelRepository vipLevelRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @Value("${canteen.ai.api-key:}")
    private String apiKey;

    @Value("${canteen.ai.base-url:https://api.apimart.ai/v1}")
    private String baseUrl;

    @Value("${canteen.ai.model:gemini-3-flash-preview-apimart}")
    private String model;

    @Value("${canteen.ai.proxy-host:}")
    private String proxyHost;

    @Value("${canteen.ai.proxy-port:0}")
    private int proxyPort;

    public AiRecommendService(DishRepository dishRepository, OrderRepository orderRepository,
                              VipLevelRepository vipLevelRepository,
                              ObjectMapper objectMapper,
                              @Value("${canteen.ai.proxy-host:}") String proxyHost,
                              @Value("${canteen.ai.proxy-port:0}") int proxyPort) {
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.vipLevelRepository = vipLevelRepository;
        this.objectMapper = objectMapper;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.restTemplate = buildRestTemplate();
    }

    // ==================== 非流式推荐（保留兼容） ====================

    public Map<String, Object> recommend(String query, List<Map<String, Object>> cart,
                                         List<Map<String, String>> history, Long userId) {
        List<DishVO> allDishes = loadAllDishes();

        if (allDishes.isEmpty()) {
            return Map.of("content", "抱歉，当前没有可用菜品。", "dishes", List.of());
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI API Key 未配置，使用关键词匹配回退");
            return fallbackRecommend(query, allDishes);
        }

        try {
            String systemPrompt = buildSystemPrompt(allDishes, cart, userId);
            List<Map<String, String>> messages = buildMessages(systemPrompt, query, history);
            String aiResponse = callAiApi(messages);
            return parseAiResponse(aiResponse, allDishes);
        } catch (Exception e) {
            log.error("AI 推荐调用失败，回退到关键词匹配: {}", e.getMessage());
            return fallbackRecommend(query, allDishes);
        }
    }

    // ==================== 流式推荐（SSE） ====================

    public SseEmitter streamRecommend(String query, List<Map<String, Object>> cart,
                                      List<Map<String, String>> history, Long userId) {
        SseEmitter emitter = new SseEmitter(120_000L);

        List<DishVO> allDishes = loadAllDishes();

        if (allDishes.isEmpty()) {
            sendSseAndComplete(emitter, Map.of(
                    "type", "result",
                    "content", "抱歉，当前没有可用菜品。",
                    "dishes", List.of()));
            return emitter;
        }

        if (apiKey == null || apiKey.isBlank()) {
            sendSseAndComplete(emitter, buildSseResult(
                    fallbackRecommend(query, allDishes)));
            return emitter;
        }

        String systemPrompt = buildSystemPrompt(allDishes, cart, userId);
        List<Map<String, String>> messages = buildMessages(systemPrompt, query, history);
        // 复制菜品列表到本地变量，避免事务关闭后访问
        List<DishVO> dishesCopy = new ArrayList<>(allDishes);

        sseExecutor.submit(() -> {
            try {
                streamAiApi(messages, dishesCopy, emitter);
            } catch (Exception e) {
                log.error("AI 流式推荐失败: {}", e.getMessage());
                try {
                    Map<String, Object> fb = fallbackRecommend(query, dishesCopy);
                    sendSseEvent(emitter, buildSseResult(fb));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));

        return emitter;
    }

    // ==================== 内部方法 ====================

    private List<Map<String, String>> buildMessages(String systemPrompt, String query,
                                                    List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 添加历史对话上下文（最多保留最近10轮）
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 20);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                String role = msg.getOrDefault("role", "");
                String content = msg.getOrDefault("content", "");
                if (!content.isBlank() && ("user".equals(role) || "assistant".equals(role))) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }

        // 当前用户消息
        messages.add(Map.of("role", "user", "content", query));
        return messages;
    }

    @SuppressWarnings("unchecked")
    private String callAiApi(List<Map<String, String>> messages) {
        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("stream", false);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);

        return extractContentFromResponse(response.getBody());
    }

    private void streamAiApi(List<Map<String, String>> messages, List<DishVO> allDishes,
                             SseEmitter emitter) throws Exception {
        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("stream", true);
        requestBody.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 使用 HttpURLConnection 处理流式响应
        Proxy proxy = resolveProxy();
        URL apiUrl = new URI(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) (proxy != null
                ? apiUrl.openConnection(proxy)
                : apiUrl.openConnection());

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(60_000);

        conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        conn.getOutputStream().flush();

        int status = conn.getResponseCode();
        if (status != 200) {
            String errBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException(status + " " + conn.getResponseMessage() + ": " + errBody);
        }

        StringBuilder fullContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if ("[DONE]".equals(data)) break;

                try {
                    JsonNode chunk = objectMapper.readTree(data);
                    JsonNode delta = extractDelta(chunk);
                    if (delta != null && delta.has("content")) {
                        String token = delta.get("content").asText("");
                        if (!token.isEmpty()) {
                            fullContent.append(token);
                            // 发送文本片段
                            sendSseEvent(emitter, Map.of("type", "token", "token", token));
                        }
                    }
                } catch (Exception e) {
                    // 忽略无法解析的 chunk
                }
            }
        } finally {
            conn.disconnect();
        }

        // 流结束，解析完整内容并发送推荐菜品
        Map<String, Object> parsed = parseAiResponse(fullContent.toString(), allDishes);
        sendSseEvent(emitter, buildSseResult(parsed));
        emitter.complete();
    }

    private JsonNode extractDelta(JsonNode chunk) {
        JsonNode choices = chunk.has("choices") ? chunk.get("choices")
                : (chunk.has("data") && chunk.get("data").has("choices"))
                ? chunk.get("data").get("choices") : null;
        if (choices != null && choices.isArray() && !choices.isEmpty()) {
            return choices.get(0).get("delta");
        }
        return null;
    }

    private String extractContentFromResponse(JsonNode body) {
        if (body == null) throw new RuntimeException("AI API 返回空响应");

        JsonNode choices;
        if (body.has("data") && body.get("data").has("choices")) {
            choices = body.get("data").get("choices");
        } else if (body.has("choices")) {
            choices = body.get("choices");
        } else {
            throw new RuntimeException("AI API 响应格式异常: " + body);
        }

        if (!choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("AI API 返回空 choices");
        }

        return choices.get(0).get("message").get("content").asText();
    }

    private void sendSseEvent(SseEmitter emitter, Map<String, Object> data) {
        try {
            emitter.send(SseEmitter.event().data(data, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("SSE send failed: {}", e.getMessage());
        }
    }

    private void sendSseAndComplete(SseEmitter emitter, Map<String, Object> data) {
        sseExecutor.submit(() -> {
            try {
                sendSseEvent(emitter, data);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }

    private Map<String, Object> buildSseResult(Map<String, Object> recommend) {
        Map<String, Object> result = new HashMap<>(recommend);
        result.put("type", "result");
        return result;
    }

    // ==================== 代理 & HTTP 构建 ====================

    private Proxy resolveProxy() {
        String pHost = (proxyHost != null && !proxyHost.isBlank()) ? proxyHost : null;
        int pPort = proxyPort;

        if (pHost == null) {
            String sysPHost = System.getProperty("https.proxyHost",
                    System.getProperty("http.proxyHost"));
            String sysPPort = System.getProperty("https.proxyPort",
                    System.getProperty("http.proxyPort"));
            if (sysPHost != null && sysPPort != null) {
                pHost = sysPHost;
                pPort = Integer.parseInt(sysPPort);
            }
        }

        if (pHost != null && pPort > 0) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pHost, pPort));
        }
        return null;
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(60_000);

        Proxy proxy = resolveProxy();
        if (proxy != null) {
            log.info("AI API 使用代理: {}", proxy.address());
            factory.setProxy(proxy);
        }

        return new RestTemplate(factory);
    }

    // ==================== 菜品加载 & 转换 ====================

    private List<DishVO> loadAllDishes() {
        return dishRepository.findAvailableByCategoryId(null)
                .stream().map(this::toDishVO).toList();
    }

    private DishVO toDishVO(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setName(dish.getName());
        vo.setDescription(dish.getDescription());
        vo.setPrice(dish.getPrice());
        vo.setImageUrl(dish.getImageUrl());
        vo.setImage(dish.getImageUrl());
        MenuCategory cat = dish.getCategory();
        vo.setCategory(cat != null ? cat.getName() : null);
        vo.setCategoryId(cat != null ? cat.getId() : null);
        vo.setSales(dish.getSales());
        vo.setStock(dish.getStock());
        return vo;
    }

    // ==================== 提示词构建 ====================

    private String buildSystemPrompt(List<DishVO> dishes, List<Map<String, Object>> cart, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「幸福社区食堂」的AI智能助手，热情友好，回答简洁。你既能推荐菜品，也能回答关于食堂的常见问题，还能帮用户跳转到对应功能页面。\n\n");

        // ---- 菜品列表 ----
        sb.append("## 当前可用菜品\n");
        for (DishVO d : dishes) {
            sb.append(String.format("- ID:%d 「%s」 ¥%.1f %s%s\n",
                    d.getId(),
                    d.getName(),
                    d.getPrice() != null ? d.getPrice().doubleValue() : 0,
                    d.getCategory() != null ? "[" + d.getCategory() + "]" : "",
                    d.getDescription() != null ? " - " + d.getDescription() : ""));
        }

        // ---- 购物车 ----
        if (cart != null && !cart.isEmpty()) {
            sb.append("\n## 用户购物车已有\n");
            for (Map<String, Object> item : cart) {
                String name = (String) item.getOrDefault("name", "");
                Object qty = item.getOrDefault("quantity", 1);
                sb.append(String.format("- %s x%s\n", name, qty));
            }
        }

        // ---- 用户历史订单偏好 ----
        if (userId != null) {
            try {
                List<Object[]> dishHistory = orderRepository.countUserDishHistory(userId);
                if (!dishHistory.isEmpty()) {
                    sb.append("\n## 用户历史消费偏好（按购买次数排序）\n");
                    int count = 0;
                    for (Object[] row : dishHistory) {
                        if (count++ >= 10) break;
                        sb.append(String.format("- %s x%s次\n", row[0], row[1]));
                    }
                    sb.append("(请根据用户偏好优先推荐类似风格的菜品,并在文案中体现个性化,如'根据您的消费习惯,推荐...')\n");
                }
            } catch (Exception e) {
                log.warn("加载用户历史订单失败: {}", e.getMessage());
            }
        }

        // ---- FAQ 知识库 ----
        sb.append("\n## 常见问题知识库\n");
        sb.append("当用户询问以下问题时，直接根据知识库回答，不需要推荐菜品：\n");
        sb.append("- **会员积分怎么用**：每消费1元可获得10积分（向下取整），积分可在「积分兑换」页面兑换优惠券使用。\n");
        sb.append("- **积分怎么获得**：每笔订单完成后自动发放积分，每消费1元得10积分。\n");
        sb.append("- **预存款能退吗**：预存余额仅限在食堂内消费使用，暂不支持退款提现。\n");

        // VIP 等级信息从数据库动态加载
        try {
            List<VipLevel> vipLevels = vipLevelRepository.findAllByOrderByLevelAsc();
            if (!vipLevels.isEmpty()) {
                StringBuilder vipDesc = new StringBuilder();
                StringBuilder upgradeDesc = new StringBuilder();
                vipDesc.append(String.format("VIP共%d个等级，等级越高折扣越大。", vipLevels.size()));
                for (VipLevel vl : vipLevels) {
                    if (vl.getDiscount() == null) continue;
                    double disc = vl.getDiscount().doubleValue();
                    if (disc >= 1.0) {
                        vipDesc.append(String.format("%d级无折扣", vl.getLevel()));
                    } else {
                        // 0.98 -> "98折", 0.88 -> "88折"
                        String discStr = String.valueOf(Math.round(disc * 100));
                        if (discStr.endsWith("0")) discStr = discStr.substring(0, discStr.length() - 1);
                        vipDesc.append(String.format("%d级%s折", vl.getLevel(), discStr));
                    }
                    vipDesc.append("，");
                    if (vl.getLevel() > 0 && vl.getMinSpend() != null) {
                        upgradeDesc.append(String.format("累计消费满%s元升%d级，",
                                vl.getMinSpend().stripTrailingZeros().toPlainString(), vl.getLevel()));
                    }
                }
                // 去掉尾部逗号
                if (!vipDesc.isEmpty()) vipDesc.setLength(vipDesc.length() - 1);
                if (!upgradeDesc.isEmpty()) upgradeDesc.setLength(upgradeDesc.length() - 1);
                sb.append("- **VIP等级有什么用**：").append(vipDesc).append("。累计消费达到对应门槛自动升级。\n");
                sb.append("- **VIP怎么升级**：").append(upgradeDesc).append("。每笔订单完成后自动检测升级。\n");
            } else {
                sb.append("- **VIP等级有什么用**：暂无VIP等级配置，请联系管理员。\n");
            }
        } catch (Exception e) {
            log.warn("加载VIP等级配置失败: {}", e.getMessage());
            sb.append("- **VIP等级有什么用**：VIP等级信息暂时无法获取，请联系客服咨询。\n");
        }

        sb.append("- **优惠券怎么用**：结算时选择可用优惠券即可抵扣，每笔订单限用一张，需满足最低消费金额。\n");
        sb.append("- **怎么充值/余额充值**：在「我的」页面点击余额，进入充值页面选择金额和支付方式即可。\n");
        sb.append("- **配送范围/配送费**：目前支持食堂周边配送，具体以下单时显示为准。\n");
        sb.append("- **营业时间**：每天 7:00-20:00，节假日正常营业。\n");
        sb.append("- **怎么联系客服**：在「我的」页面点击「联系客服」即可。\n");

        // ---- 页面导航指令 ----
        sb.append("""
                
                ## 页面导航功能
                当用户明确表达想要使用某个功能（如"我要充值""帮我看看积分""去兑换优惠券"）时，在回复中附带对应的 action 指令，帮助用户快速跳转。
                可用的 action 值：
                - "points" → 积分兑换页面（用户想用积分、兑换优惠券时）
                - "coupons" → 我的优惠券页面（用户想查看优惠券时）
                - "recharge" → 余额充值页面（用户想充值时）
                - "orders" → 我的订单页面（用户想看订单、查物流时）
                - "vip-recharge" → 开通/续费会员页面（用户想开通VIP时）
                - "records" → 流水明细页面（用户想看消费记录时）
                - "address" → 收货地址页面（用户想管理地址时）
                - "settings" → 设置页面
                注意：只有用户明确表达要"去""打开""使用"某功能时才返回 action，单纯询问信息不需要 action。
                """);

        // ---- 任务与输出格式 ----
        sb.append("""
                
                ## 你的任务
                根据用户消息的意图，判断属于以下哪种情况并做出对应回复：
                1. **点餐推荐**：用户想吃东西或请你推荐菜品 → 推荐1~3道菜品，附 dishIds
                2. **常见问题**：用户在问食堂相关的问题 → 直接回答，dishIds 为空数组
                3. **功能跳转**：用户想使用某个功能 → 简短回复并附 action 字段
                4. **混合场景**：如用户说"推荐个菜，顺便告诉我积分怎么用" → 同时推荐菜品和回答问题
                
                ## 输出格式
                严格要求：
                1. 直接输出纯 JSON，禁止用 ```json 或任何 markdown 代码块包裹
                2. content 字段内部禁止使用英文双引号 " ，如需引用请用「」或单引号
                3. action 不需要时写 null（不带引号）
                
                格式：{"content":"你的回复文案","dishIds":[菜品ID列表],"action":null}
                
                示例：
                - 推荐菜品：{"content":"根据您的消费习惯，推荐我们的招牌红烧肉套餐！","dishIds":[1,3],"action":null}
                - 回答问题：{"content":"会员积分每消费1元可获得10积分，您可以在「积分兑换」页面用积分兑换优惠券哦！","dishIds":[],"action":null}
                - 功能跳转：{"content":"好的，马上为您打开积分兑换页面~","dishIds":[],"action":"points"}
                - 混合回复：{"content":"给您推荐红烧肉！另外积分可以兑换优惠券哦~","dishIds":[1],"action":null}
                """);

        return sb.toString();
    }

    // ==================== AI 响应解析 ====================

    private Map<String, Object> parseAiResponse(String aiText, List<DishVO> allDishes) {
        String jsonStr = extractJson(aiText);

        Map<Long, DishVO> dishMap = allDishes.stream()
                .collect(Collectors.toMap(DishVO::getId, d -> d, (a, b) -> a));

        try {
            JsonNode json = objectMapper.readTree(jsonStr);
            String content = json.has("content") ? json.get("content").asText() : "为您推荐以下菜品：";
            List<Long> dishIds = new ArrayList<>();
            if (json.has("dishIds") && json.get("dishIds").isArray()) {
                for (JsonNode idNode : json.get("dishIds")) {
                    dishIds.add(idNode.asLong());
                }
            }

            // 解析 action 字段（页面跳转指令）
            String action = null;
            if (json.has("action") && !json.get("action").isNull()) {
                String raw = json.get("action").asText("");
                if (!raw.isBlank() && !"null".equals(raw)) {
                    action = raw;
                }
            }

            List<DishVO> recommended = dishIds.stream()
                    .map(dishMap::get)
                    .filter(Objects::nonNull)
                    .limit(3)
                    .toList();

            // 如果是纯问答/导航（没有推荐菜品），不回退到默认菜品
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("dishes", recommended);
            if (action != null) {
                result.put("action", action);
            }
            return result;
        } catch (Exception e) {
            log.warn("解析 AI JSON 失败，尝试正则回退: {}", e.getMessage());
            // 二次尝试：用正则从原始文本中提取字段
            Map<String, Object> regexResult = regexFallbackParse(aiText);
            if (regexResult != null) {
                String rc = (String) regexResult.get("content");
                @SuppressWarnings("unchecked")
                List<Long> rIds = (List<Long>) regexResult.getOrDefault("dishIds", List.of());
                List<DishVO> recommended = rIds.stream()
                        .map(dishMap::get).filter(Objects::nonNull).limit(3).toList();
                Map<String, Object> result = new HashMap<>();
                result.put("content", rc);
                result.put("dishes", recommended);
                if (regexResult.containsKey("action")) result.put("action", regexResult.get("action"));
                return result;
            }
            // 最终兜底：返回纯文本，不附带随机菜品
            String fallbackContent = aiText.replaceAll("```[a-z]*\\n?", "").replace("```", "").trim();
            if (fallbackContent.length() > 300) fallbackContent = fallbackContent.substring(0, 300);
            Map<String, Object> result = new HashMap<>();
            result.put("content", fallbackContent);
            result.put("dishes", List.of());
            return result;
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();

        // 剥离 markdown 代码块（```json ... ``` 或 ``` ... ```）
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastBacktick = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastBacktick > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastBacktick).trim();
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String json = trimmed.substring(start, end + 1);
            // 修复 AI 在 content 值中嵌入未转义双引号的问题
            // 策略：如果直接解析失败，尝试用正则提取各字段值
            return json;
        }

        return trimmed;
    }

    /**
     * 当 JSON 直接解析失败时，尝试用正则提取字段
     */
    private Map<String, Object> regexFallbackParse(String aiText) {
        Map<String, Object> result = new HashMap<>();
        // 提取 content（贪婪匹配到 ","dishIds" 之前）
        java.util.regex.Matcher cm = java.util.regex.Pattern
                .compile("\"content\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"dishIds\"", java.util.regex.Pattern.DOTALL)
                .matcher(aiText);
        String content = cm.find() ? cm.group(1) : null;

        // 提取 dishIds
        java.util.regex.Matcher dm = java.util.regex.Pattern
                .compile("\"dishIds\"\\s*:\\s*\\[(.*?)\\]")
                .matcher(aiText);
        List<Long> dishIds = new ArrayList<>();
        if (dm.find()) {
            String ids = dm.group(1).trim();
            if (!ids.isEmpty()) {
                for (String id : ids.split(",")) {
                    try { dishIds.add(Long.parseLong(id.trim())); } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 提取 action
        java.util.regex.Matcher am = java.util.regex.Pattern
                .compile("\"action\"\\s*:\\s*\"([a-z\\-]+)\"")
                .matcher(aiText);
        String action = am.find() ? am.group(1) : null;

        if (content != null) {
            result.put("content", content);
            result.put("dishIds", dishIds);
            if (action != null) result.put("action", action);
            return result;
        }
        return null; // 正则也无法提取
    }

    // ==================== Fallback ====================

    private Map<String, Object> fallbackRecommend(String query, List<DishVO> allDishes) {
        List<DishVO> matched = allDishes.stream()
                .filter(d -> (d.getName() != null && d.getName().contains(query))
                        || (d.getDescription() != null && d.getDescription().contains(query)))
                .limit(3)
                .toList();

        String content;
        if (matched.isEmpty()) {
            matched = allDishes.stream().limit(3).toList();
            content = "为您推荐以下热门菜品，要不要尝尝？";
        } else {
            content = "根据您的需求，为您推荐以下菜品：";
        }

        return Map.of("content", content, "dishes", matched);
    }
}
