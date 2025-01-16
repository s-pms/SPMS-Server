package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
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

/**
 * <h1>配置信息实体</h1>
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
    private String flag;

    @Description("配置名称")
    @Column(columnDefinition = "varchar(255) default '' comment '配置名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "配置名称不能为空")
    private String name;

    @Description("配置的值")
    @Column(columnDefinition = "varchar(255) default '' comment '字符串值'")
    private String config;

    @Description("配置类型")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '配置类型'")
    @Dictionary(value = ConfigType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("内置配置")
    @Search(Search.Mode.EQUALS)
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '内置配置'")
    private Boolean isSystem;

    /**
     * <h3>设置是系统内置配置</h3>
     *
     * @param isSystem 内置配置
     * @return 配置信息
     */
    public ConfigEntity setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Boolean booleanConfig() {
        if (Objects.isNull(config)) {
            return false;
        }
        return Constant.ONE_STRING.equals(config);
    }

    @Transient
    public Long numberConfig() {
        if (Objects.isNull(config)) {
            return Constant.ZERO_LONG;
        }
        return Long.parseLong(config);
    }
}
