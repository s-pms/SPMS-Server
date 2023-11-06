package com.qqlab.spms.module.system.permission;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseTreeEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
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
     * <h2>权限标识</h2>
     */
    @Description("权限标识")
    @Column(columnDefinition = "varchar(255) default '' comment '权限标识'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "权限标识不能为空")
    private String identity;

    /**
     * <h2>系统权限</h2>
     */
    @Description("系统权限")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '系统权限'")
    private Boolean isSystem;

    /**
     * <h2>子菜单</h2>
     */
    @Transient
    private List<PermissionEntity> children;
}
