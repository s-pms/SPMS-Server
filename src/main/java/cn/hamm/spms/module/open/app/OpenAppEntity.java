package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.airpower.curd.annotation.Search;
import cn.hamm.airpower.open.IOpenApp;
import cn.hamm.airpower.open.OpenArithmeticType;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.open.oauth.IOauthAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

/**
 * <h1>实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "open_app")
@Description("开放应用")
public class OpenAppEntity extends BaseEntity<OpenAppEntity> implements IOpenAppAction, IOpenApp, IOauthAction {
    @Description("应用 Key")
    @Column(columnDefinition = "varchar(255) default '' comment 'AppKey'", unique = true)
    @Search
    @Meta
    private String appKey;

    @Description("应用密钥")
    @JsonProperty(access = WRITE_ONLY)
    @Column(columnDefinition = "varchar(255) default '' comment 'AppSecret'")
    private String appSecret;

    @Description("应用名称")
    @Column(columnDefinition = "varchar(255) default '' comment '应用名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "应用名称不能为空")
    @Search
    @Meta
    private String appName;

    @Description("加密算法")
    @Dictionary(value = OpenArithmeticType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "int UNSIGNED default 1 comment '加密算法'")
    private Integer arithmetic;

    @Description("IP 白名单")
    @Column(columnDefinition = "text comment 'IP白名单'")
    private String ipWhiteList;

    @Description("公钥")
    @ReadOnly
    @JsonProperty(access = WRITE_ONLY)
    @Column(columnDefinition = "text comment '公钥'")
    private String publicKey;

    @Description("私钥")
    @ReadOnly
    @JsonProperty(access = WRITE_ONLY)
    @Column(columnDefinition = "text comment '私钥'")
    private String privateKey;

    @Description("应用地址")
    @Column(columnDefinition = "varchar(255) default '' comment '应用地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用地址必须填写")
    private String url;

    @Description("是否内部应用")
    @Column(columnDefinition = "bit(1) default 0 comment '是否内部应用'")
    private Boolean isInternal;
}
