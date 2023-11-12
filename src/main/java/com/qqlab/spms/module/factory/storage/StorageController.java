package com.qqlab.spms.module.factory.storage;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("storage")
@Description("存储资源")
@Permission(login = false)
public class StorageController extends BaseController<StorageEntity, StorageService, StorageRepository> {
}
