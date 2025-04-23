package cn.hamm.spms.module.mcp;

import cn.hamm.airpower.access.AccessTokenUtil;
import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.ApiController;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.airpower.mcp.McpService;
import cn.hamm.airpower.mcp.model.McpRequest;
import cn.hamm.airpower.mcp.model.McpResponse;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.exception.ServiceError.PARAM_MISSING;

/**
 * <h1>MCP</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@Api("mcp")
@Slf4j
public class McpController extends ApiController {
    @Autowired
    private McpService mcpService;

    @Autowired
    private RequestInterceptor requestInterceptor;

    @PostMapping("")
    public McpResponse messages(HttpServletRequest request, @RequestBody McpRequest mcpRequest) {
        try {
            String accessToken = request.getParameter("token");
            PARAM_MISSING.whenNull(accessToken, "accessToken is required");
            AccessTokenUtil.VerifiedToken verifiedToken = requestInterceptor.getVerifiedToken(accessToken);
            return mcpService.run(mcpRequest, (mcpTool -> {
                long userId = verifiedToken.getPayloadId();
                requestInterceptor.checkUserPermission(userId, McpService.getPermissionIdentity(mcpTool), request);
            }));
        } catch (ServiceException e) {
            McpResponse response = new McpResponse().setError(new McpResponse.McpError(e));
            response.setId(mcpRequest.getId());
            return response;
        }
    }

    @PostMapping("getMcpTools")
    @Description("获取MCP工具列表")
    public Json messages() {
        return Json.data(McpService.tools);
    }
}
