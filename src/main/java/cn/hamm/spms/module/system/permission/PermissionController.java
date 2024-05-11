package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("permission")
@Description("权限")
public class PermissionController extends BaseController<PermissionEntity, PermissionService, PermissionRepository> {

    @Override
    protected PermissionEntity beforeAdd(@NotNull PermissionEntity entity) {
        return entity.setIsSystem(null);
    }

    @Override
    protected PermissionEntity beforeUpdate(@NotNull PermissionEntity entity) {
        return entity.setIsSystem(null);
    }

    @Override
    protected List<PermissionEntity> afterGetList(List<PermissionEntity> list) {
        return Utils.getTreeUtil().buildTreeList(list);
    }
}
