package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.interceptor.ResponseBodyInterceptor;
import cn.hamm.spms.module.system.log.LogEntity;
import cn.hamm.spms.module.system.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <h1>请求拦截器</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ResponseInterceptor extends ResponseBodyInterceptor {
    @Autowired
    private LogService logService;

    @Override
    protected Object beforeResponseFinished(Object body, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            Object shareData = getShareData(RequestInterceptor.LOG_REQUEST_KEY);
            if (Objects.isNull(shareData)) {
                return body;
            }
            long logId = Long.parseLong(shareData.toString());
            LogEntity log = logService.getMaybeNull(logId);
            if (Objects.nonNull(log)) {
                String bodyString = body.toString();
                try {
                    bodyString = Json.toString(body);
                } catch (Exception ignored) {

                }
                log.setResponse(bodyString);
                logService.update(log);
            }
        } catch (Exception ignored) {

        }
        return body;
    }
}
