package cn.hamm.spms.common.interceptor;

import cn.hamm.airpower.interceptor.ResponseBodyInterceptor;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.system.log.LogEntity;
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
    @Override
    protected Object beforeResponseFinished(Object body, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            Object shareData = getShareData(RequestInterceptor.LOG_REQUEST_KEY);
            if (Objects.isNull(shareData)) {
                return body;
            }
            long logId = Long.parseLong(shareData.toString());
            LogEntity log = Services.getLogService().getMaybeNull(logId);
            if (Objects.nonNull(log)) {
                String bodyString = body.toString();
                try {
                    bodyString = Json.toString(body);
                } catch (java.lang.Exception ignored) {

                }
                log.setResponse(bodyString);
                Services.getLogService().update(log);
            }
        } catch (java.lang.Exception ignored) {

        }
        return body;
    }
}
