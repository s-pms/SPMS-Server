package cn.hamm.spms.module.personnel.user.department;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("user/department")
@Description("用户部门")
public class UserDepartmentController extends BaseController<UserDepartmentEntity, UserDepartmentService, UserDepartmentRepository> {
}
