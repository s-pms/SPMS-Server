package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("storage")
@Description("仓库")
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
