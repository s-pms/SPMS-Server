package com.qqlab.spms.module.system.app;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Search;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>系统应用实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "app")
@Description("应用")
public class AppEntity extends BaseEntity<AppEntity> {
    /**
     * 应用Key
     */
    @Column(columnDefinition = "varchar(255) default '' comment 'AppKey'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class, WhenCode2AccessToken.class}, message = "AppKey必须填写")
    private String appKey;

    /**
     * 应用密钥
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "varchar(255) default '' comment 'AppSecret'", unique = true)
    @Null(groups = {WhenAdd.class, WhenUpdate.class}, message = "请不要传入AppSecret")
    @NotBlank(groups = {WhenCode2AccessToken.class})
    @Exclude(filters = {WhenGetDetail.class})
    private String appSecret;

    /**
     * 应用名称
     */
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '应用名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用名称必须填写")
    private String appName;

    /**
     * 应用地址
     */
    @Column(columnDefinition = "varchar(255) default '' comment '应用地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用地址必须填写")
    private String url;

    /**
     * 临时code
     */
    @Description("临时码")
    @NotBlank(groups = {AppEntity.WhenCode2AccessToken.class})
    @NotBlank(groups = {AppEntity.WhenAccessToken.class}, message = "Code不能为空")
    @Transient
    private String code;

    /**
     * Cookie
     */
    @Transient
    private String cookie;

    public interface WhenCode2AccessToken {
    }

    public interface WhenAccessToken {
    }

    public interface WhenGetByAppKey {
    }

    public interface WhenResetSecret {
    }

}
