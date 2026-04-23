package cn.hamm.spms.module.personnel.department;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class DepartmentService extends BaseService<DepartmentEntity, DepartmentRepository> {
    /**
     * 排序字段
     */
    static final String ORDER_FIELD_NAME = "orderNo";

    @Override
    protected void beforeDelete(@NotNull DepartmentEntity department) {
        TreeUtil.ensureNoChildrenBeforeDelete(department.getId(), (id) -> filter(new DepartmentEntity().setId(id)));
    }

    @Override
    protected @NotNull List<DepartmentEntity> afterGetList(@NotNull List<DepartmentEntity> list) {
        list.forEach(DepartmentEntity::excludeNotMeta);
        return list;
    }

    @Override
    protected DepartmentEntity beforeAppSaveToDatabase(@NotNull DepartmentEntity department) {
        DepartmentEntity filter = new DepartmentEntity().setParentId(department.getParentId()).setName(department.getName());
        List<DepartmentEntity> exists = filter(filter);
        if (Objects.nonNull(department.getId())) {
            // 有ID 编辑 不允许有同名的部门 且ID不是自己的部门
            FORBIDDEN_EXIST.when(!exists.isEmpty() && !Objects.equals(exists.get(0).getId(), department.getId()), "同级别下部门不允许重复");
        } else {
            FORBIDDEN_EXIST.when(!exists.isEmpty(), "同级别下部门已有同名部门");
        }
        return department;
    }

    /**
     * 递归获取子部门
     *
     * @param parentId      父级ID
     * @param departmentIds 子部门列表
     * @return 子部门列表
     */
    @Contract("_, _ -> param2")
    private Set<Long> getListByParentId(long parentId, @NotNull Set<Long> departmentIds) {
        DepartmentEntity parent = get(parentId);
        List<DepartmentEntity> children = filter(new DepartmentEntity().setParentId(parent.getId()));
        children.forEach(child -> getListByParentId(child.getId(), departmentIds));
        return departmentIds;
    }

    /**
     * 递归获取子部门
     *
     * @param parentId 父级ID
     * @return 子部门列表
     */
    @Contract("_ -> new")
    public @NotNull Set<Long> getListByParentId(long parentId) {
        return getListByParentId(parentId, new HashSet<>());
    }
}
