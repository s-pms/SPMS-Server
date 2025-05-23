package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("storage")
@Description("仓库")
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
