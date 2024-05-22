package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("storage")
@Description("存储资源")
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
