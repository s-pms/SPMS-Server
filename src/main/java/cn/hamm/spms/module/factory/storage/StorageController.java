package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>仓库</h1>
 *
 * @author Hamm.cn
 */
@Api("storage")
@Description("仓库")
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
