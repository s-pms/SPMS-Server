package cn.hamm.spms.module.personnel.department;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.model.Json;
import cn.hamm.airpower.core.model.tree.TreeUtil;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Extends;
import cn.hamm.airpower.web.annotation.Permission;
import cn.hamm.airpower.web.model.query.QueryListRequest;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.web.enums.Api.Export;
import static cn.hamm.airpower.web.enums.Api.QueryExport;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("department")
@Description("部门")
@Extends(exclude = {Export, QueryExport})
public class DepartmentController extends BaseController<DepartmentEntity, DepartmentService, DepartmentRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(@RequestBody QueryListRequest<DepartmentEntity> queryListRequest) {
        return Json.data(TreeUtil.buildTreeList(service.getList(queryListRequest)));
    }
}
