package cn.hamm.spms.module.personnel.user.role;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("user/role")
@Description("用户角色")
public class UserRoleController extends BaseController<UserRoleEntity, UserRoleService, UserRoleRepository> {
}
