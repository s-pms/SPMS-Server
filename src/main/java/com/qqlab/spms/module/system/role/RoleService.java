package com.qqlab.spms.module.system.role;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class RoleService extends RootService<RoleEntity, RoleRepository> {
    @Override
    protected void beforeDelete(RoleEntity entity) {
        Result.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置角色无法被删除!");
    }
}
