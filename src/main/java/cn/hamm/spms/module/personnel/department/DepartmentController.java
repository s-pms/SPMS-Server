package cn.hamm.spms.module.personnel.department;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.airpower.curd.model.query.QueryListRequest;
import cn.hamm.airpower.curd.model.query.Sort;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

import static cn.hamm.airpower.curd.base.Curd.Export;
import static cn.hamm.airpower.curd.base.Curd.QueryExport;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("department")
@Description("部门")
@Extends(exclude = {Export, QueryExport})
public class DepartmentController extends BaseController<DepartmentEntity, DepartmentService, DepartmentRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(@RequestBody @NotNull QueryListRequest<DepartmentEntity> queryListRequest) {
        DepartmentEntity filter = queryListRequest.getFilter();
        queryListRequest.setSort(Objects.requireNonNullElse(
                queryListRequest.getSort(),
                new Sort().setField(DepartmentService.ORDER_FIELD_NAME)
        ));
        queryListRequest.setFilter(filter);
        return Json.data(TreeUtil.buildTreeList(service.getList(queryListRequest)));
    }
}
