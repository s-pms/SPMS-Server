package cn.hamm.spms.module.personnel.department;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_EXIST;

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
}
