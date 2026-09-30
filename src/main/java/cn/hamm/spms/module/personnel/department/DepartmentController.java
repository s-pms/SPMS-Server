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
import cn.hamm.spms.common.AppConstant;
import cn.hamm.spms.module.personnel.PersonnelServices;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hamm.airpower.curd.base.Curd.Export;
import static cn.hamm.airpower.curd.base.Curd.QueryExport;

/**
 * <h1>部门</h1>
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
        List<DepartmentEntity> list = service.getList(queryListRequest);
        return Json.data(TreeUtil.buildTreeList(limitVisibility(list)));
    }

    /**
     * 限制可见的部门范围
     *
     * @param list 全量部门列表
     * @return 当前用户可见的部门列表
     * @apiNote 供 {@code getList} 调用。本接口免权限（前端各处都要用部门选择器），
     * 不能返回全公司组织树，否则任意登录用户都能拿到完整组织架构。
     * 超管返回全部，普通用户只返回自己所在部门及其子孙部门
     */
    private @NotNull List<DepartmentEntity> limitVisibility(@NotNull List<DepartmentEntity> list) {
        long currentUserId = getCurrentUserId();
        if (currentUserId == AppConstant.ROOT_USER_ID) {
            return list;
        }
        UserEntity currentUser = PersonnelServices.getUserService().get(currentUserId);
        Set<DepartmentEntity> departmentList = currentUser.getDepartmentList();
        if (Objects.isNull(departmentList) || departmentList.isEmpty()) {
            return List.of();
        }
        Set<Long> ownDepartmentIds = departmentList.stream()
                .map(DepartmentEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ownDepartmentIds.isEmpty()) {
            return List.of();
        }
        Set<Long> visibleIds = service.getVisibleDepartmentIds(ownDepartmentIds);
        return list.stream().filter(d -> visibleIds.contains(d.getId())).toList();
    }
}
