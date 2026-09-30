package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.system.config.enums.ConfigType;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.util.Objects;

import static cn.hamm.spms.module.system.config.ConfigService.STRING_ONE;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

/**
 * <h1>系统配置</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "config")
@Description("配置信息")
public class ConfigEntity extends BaseEntity<ConfigEntity> {
    @Description("配置标识")
    @Column(columnDefinition = "varchar(255) comment '规则字段'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "配置标识不能为空")
    @Meta
    private String flag;

    @Description("配置名称")
    @Column(columnDefinition = "varchar(255) default '' comment '配置名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "配置名称不能为空")
    @Meta
    private String name;

    @Description("配置描述")
    @Column(columnDefinition = "varchar(255) default '' comment '配置描述'")
    private String description;

    @Description("配置的值")
    @Column(columnDefinition = "varchar(255) default '' comment '字符串值'")
    @Meta
    private String config;

    @Description("配置类型")
    @Column(columnDefinition = "int UNSIGNED default 0 comment '配置类型'")
    @Dictionary(value = ConfigType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Meta
    private Integer type;

    @Description("内置配置")
    @ReadOnly
    @Column(columnDefinition = "bit(1) default 0 comment '内置配置'")
    private Boolean isSystem;

    /**
     * 设置是否系统内置配置
     *
     * @param isSystem 是否内置
     * @return 配置信息
     * @apiNote Lombok 不会为包装类型 {@code Boolean} 字段生成链式 setter，这里手写
     */
    public ConfigEntity setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }

    /**
     * 读取布尔值
     *
     * @return {@code "1"} 为 {@code true}，其余（含 {@code null}）为 {@code false}
     */
    @Transient
    @JsonProperty(access = WRITE_ONLY)
    public Boolean booleanConfig() {
        if (Objects.isNull(config)) {
            return false;
        }
        return STRING_ONE.equals(config);
    }

    /**
     * 读取数值
     *
     * @return 配置值，{@code null} 时返回 0
     */
    @Transient
    public Long numberConfig() {
        if (Objects.isNull(config)) {
            return 0L;
        }
        return Long.parseLong(config);
    }
}
