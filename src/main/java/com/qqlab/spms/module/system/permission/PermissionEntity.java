package com.qqlab.spms.module.system.permission;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseTreeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

/**
 * <h1>权限实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "permission")
@Description("权限")
public class PermissionEntity extends BaseTreeEntity<PermissionEntity> {
    /**
     * 权限标识
     */
    @Description("权限标识")
    @Column(columnDefinition = "varchar(255) default '' comment '权限标识'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "权限标识不能为空")
    private String identity;

    /**
     * 系统权限
     */
    @Description("系统权限")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '系统权限'")
    private Boolean isSystem;

    /**
     * 子菜单
     */
    @Transient
    private List<PermissionEntity> children;
}
