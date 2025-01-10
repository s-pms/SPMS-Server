package cn.hamm.spms.module.personnel.user.department;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.model.query.QueryListRequest;
import cn.hamm.airpower.util.TreeUtil;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("department")
@Description("部门")
@Extends(exclude = {Api.Export, Api.QueryExport})
public class DepartmentController extends BaseController<DepartmentEntity, DepartmentService, DepartmentRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(@RequestBody QueryListRequest<DepartmentEntity> queryListRequest) {
        return Json.data(TreeUtil.buildTreeList(service.getList(queryListRequest)));
    }
}
