package com.qqlab.spms.module.personnel.role;

import cn.hamm.airpower.result.Result;
import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class RoleService extends BaseService<RoleEntity, RoleRepository> {
    @Override
    protected void beforeDelete(long id) {
        RoleEntity entity = get(id);
        Result.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置角色无法被删除!");
    }
}
