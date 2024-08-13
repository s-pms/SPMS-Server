package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.open.IOpenApp;
import cn.hamm.airpower.open.OpenArithmeticType;
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
public class OpenAppEntity extends BaseEntity<OpenAppEntity> implements IOpenAppAction, IOpenApp {
    @Description("应用Key")
    @Column(columnDefinition = "varchar(255) default '' comment 'AppKey'", unique = true)
    private String appKey;

    @Description("应用密钥")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "varchar(255) default '' comment 'AppSecret'")
    @NotBlank(groups = {WhenCode2AccessToken.class})
    @Exclude(filters = {WhenGetDetail.class})
    private String appSecret;

    @Description("应用名称")
    @Column(columnDefinition = "varchar(255) default '' comment '应用名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "应用名称不能为空")
    private String appName;

    @Description("加密算法")
    @Dictionary(value = OpenArithmeticType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '加密算法'")
    @Search(Search.Mode.EQUALS)
    private Integer arithmetic;

    @Description("公钥")
    @ReadOnly
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "text comment '公钥'")
    private String publicKey;

    @Description("私钥")
    @ReadOnly
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "text comment '私钥'")
    private String privateKey;

    @Description("IP白名单")
    @Column(columnDefinition = "text comment 'IP白名单'")
    private String ipWhiteList;

    @Description("应用地址")
    @Column(columnDefinition = "varchar(255) default '' comment '应用地址'")
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "应用地址必须填写")
    private String url;

    @Description("临时码")
    @NotBlank(groups = {WhenCode2AccessToken.class}, message = "Code不能为空")
    @Transient
    private String code;

    @Description("Cookie")
    @Transient
    private String cookie;
}
