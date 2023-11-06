package com.qqlab.spms.module.basic.storage;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
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
@Extends(exclude = Api.Delete)
public class StorageController extends RootEntityController<StorageService, StorageEntity> {
}
