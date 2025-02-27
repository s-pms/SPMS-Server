package cn.hamm.spms.module.open.log;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.model.query.QueryPageRequest;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import org.jetbrains.annotations.NotNull;

import static cn.hamm.airpower.enums.Api.GetDetail;
import static cn.hamm.airpower.enums.Api.GetPage;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("openLog")
@Description("调用日志")
@Extends({GetDetail, GetPage})
public class OpenLogController extends BaseController<OpenLogEntity, OpenLogService, OpenLogRepository> implements IOpenLogAction {
    @Override
    protected QueryPageRequest<OpenLogEntity> beforeGetPage(@NotNull QueryPageRequest<OpenLogEntity> queryPageRequest) {
        OpenAppEntity openApp = new OpenAppEntity();
        queryPageRequest.setFilter(queryPageRequest.getFilter().setOpenApp(openApp));
        return queryPageRequest;
    }
}
