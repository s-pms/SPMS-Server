package cn.hamm.spms.module.system.app;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>系统应用实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "app")
@Description("应用")
public class AppEntity extends BaseEntity<AppEntity> implements IAppAction {
    @Description("应用Key")
    @Column(columnDefinition = "varchar(255) default '' comment 'AppKey'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class, WhenCode2AccessToken.class}, message = "AppKey必须填写")
    private String appKey;

    @Description("应用密钥")
    @ReadOnly
    @Column(columnDefinition = "varchar(255) default '' comment 'AppSecret'", unique = true)
    @Null(groups = {WhenAdd.class, WhenUpdate.class}, message = "请不要传入AppSecret")
    @NotBlank(groups = {WhenCode2AccessToken.class})
    @Exclude(filters = {WhenGetDetail.class})
    private String appSecret;

    @Description("应用名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '应用名称'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用名称必须填写")
    private String appName;

    @Description("应用地址")
    @Column(columnDefinition = "varchar(255) default '' comment '应用地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用地址必须填写")
    private String url;

    @Description("临时码")
    @NotBlank(groups = {WhenCode2AccessToken.class})
    @NotBlank(groups = {WhenAccessToken.class}, message = "Code不能为空")
    @Transient
    private String code;

    @Description("Cookie")
    @Transient
    private String cookie;
}
