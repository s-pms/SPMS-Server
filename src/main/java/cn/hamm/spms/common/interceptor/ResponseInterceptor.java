package cn.hamm.spms.common.interceptor;


import cn.hamm.airpower.interceptor.ResponseBodyInterceptor;
import cn.hamm.spms.module.system.log.LogEntity;
import cn.hamm.spms.module.system.log.LogService;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <h1>响应拦截器</h1>
 *
 * @author Hamm
 */
@Component
public class ResponseInterceptor extends ResponseBodyInterceptor {
    @Autowired
    private LogService logService;

    @Override
    protected Object beforeResponseFinished(Object body, ServerHttpRequest request, ServerHttpResponse response) {
        Object logId = getShareData("logId");
        if (Objects.isNull(logId)) {
            return body;
        }
        LogEntity log = logService.getMaybeNull(Long.parseLong(logId.toString()));
        if (Objects.isNull(log)) {
            return null;
        }
        String responseBody = body.toString();
        try {
            responseBody = JSONUtil.toJsonStr(body);
        } catch (Exception ignored) {

        }
        log.setResponse(responseBody);
        logService.update(log);
        return body;
    }
}
