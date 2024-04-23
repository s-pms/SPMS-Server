package cn.hamm.spms.module.factory.storage;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("storage")
@Description("存储资源")
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
