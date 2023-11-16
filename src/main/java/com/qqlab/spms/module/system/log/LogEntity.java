package com.qqlab.spms.module.system.log;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * <h1>日志实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "log")
@Description("日志")
public class LogEntity extends BaseEntity<LogEntity> {
    /**
     * <h2>访问动作</h2>
     */
    @Column(columnDefinition = "varchar(255) default '' comment '访问动作'")
    private String action;

    /**
     * <h2>IP地址</h2>
     */
    @Column(columnDefinition = "varchar(255) default '' comment 'IP地址'")
    private String ip;

    /**
     * <h2>客户端版本</h2>
     */
    @Column(columnDefinition = "int UNSIGNED default 10000 comment '客户端版本'")
    private Integer version;

    /**
     * <h2>用户ID</h2>
     */
    @Column(columnDefinition = "bigint UNSIGNED comment '用户ID'")
    private Long userId;
}
