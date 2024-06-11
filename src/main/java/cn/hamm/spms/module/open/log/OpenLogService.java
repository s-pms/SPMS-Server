package cn.hamm.spms.module.open.log;

import cn.hamm.airpower.open.IOpenApp;
import cn.hamm.airpower.open.IOpenLogService;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class OpenLogService extends BaseService<OpenLogEntity, OpenLogRepository> implements IOpenLogService {
    /**
     * <h2>添加一个请求日志</h2>
     *
     * @param openApp     开放应用
     * @param url         请求地址
     * @param requestBody 请求体
     * @return 请求日志ID
     */
    @Override
    public Long addRequest(@NotNull IOpenApp openApp, String url, String requestBody) {
        OpenAppEntity existOpenApp = Services.getOpenAppService().getByAppKey(openApp.getAppKey());
        if (Objects.isNull(existOpenApp)) {
            return null;
        }
        return add(new OpenLogEntity().setRequest(requestBody).setUrl(url).setOpenApp(existOpenApp));
    }

    /**
     * <h2>更新请求日志</h2>
     *
     * @param openLogId    请求日志ID
     * @param responseBody 响应体
     */
    @Override
    public void updateResponse(Long openLogId, String responseBody) {
        update(get(openLogId).setResponse(responseBody));
    }
}
