package cn.hamm.spms.module.mcp;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.mcp.McpService;
import cn.hamm.airpower.mcp.exception.McpErrorCode;
import cn.hamm.airpower.mcp.exception.McpException;
import cn.hamm.airpower.mcp.method.McpMethods;
import cn.hamm.airpower.mcp.model.McpRequest;
import cn.hamm.airpower.mcp.model.McpResponse;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.util.AccessTokenUtil;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cn.hamm.airpower.exception.ServiceError.PARAM_MISSING;

/**
 * <h1>MCP</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@ApiController("mcp")
@Slf4j
public class McpController extends RootController {
    public static ConcurrentMap<String, String> sessionPersonalTokens = new ConcurrentHashMap<>();

    @Autowired
    private McpService mcpService;

    @Autowired
    private RequestInterceptor requestInterceptor;

    @GetMapping(value = "sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() throws IOException, McpException {
        String uuid = UUID.randomUUID().toString();
        SseEmitter sseEmitter = McpService.getSseEmitter(uuid);

        String token = request.getParameter("token");
        if (Objects.isNull(token)) {
            McpService.emitError(uuid, 0L, "Token is required");
            return sseEmitter;
        }
        sessionPersonalTokens.put(uuid, token);
        sseEmitter.send(SseEmitter.event()
                .name("endpoint")
                .data("/mcp/messages?sessionId=" + uuid)
        );
        return sseEmitter;
    }

    @PostMapping("messages")
    public Json messages(HttpServletRequest request, @RequestBody McpRequest mcpRequest) {
        String uuid = request.getParameter("sessionId");
        PARAM_MISSING.whenNull(uuid, "sessionId is required");

        String accessToken = sessionPersonalTokens.get(uuid);
        PARAM_MISSING.whenNull(accessToken, "accessToken is required");

        AccessTokenUtil.VerifiedToken verifiedToken = requestInterceptor.getVerifiedToken(accessToken);

        String method = mcpRequest.getMethod();
        McpResponse mcpResponse;
        try {
            if (!StringUtils.hasText(method)) {
                McpService.emitError(uuid, mcpRequest.getId(), McpErrorCode.MethodNotFound);
                return Json.error("Method not found");
            }
            McpMethods mcpMethods = Arrays.stream(McpMethods.values())
                    .filter(item -> item.getLabel().equals(method))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(mcpMethods)) {
                McpService.emitError(uuid, mcpRequest.getId(), McpErrorCode.MethodNotFound);
                return Json.error("Method not found");
            }
            mcpResponse = mcpService.run(uuid, mcpMethods, mcpRequest, mcpTool -> {
                long userId = verifiedToken.getPayloadId();
                requestInterceptor.checkUserPermission(userId, McpService.getPermissionIdentity(mcpTool), request);
            });
        } catch (McpException mcpException) {
            try {
                McpService.emitResult(uuid, mcpRequest.getId(), mcpException.getMessage());
            } catch (McpException e) {
                throw new RuntimeException(e);
            }
            return Json.error(mcpException.getMessage());
        }

        return Json.data(mcpResponse, "success");
    }
}
