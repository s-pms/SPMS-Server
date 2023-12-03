package com.qqlab.spms.module.personnel.user;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.password.Password;
import cn.hamm.airpower.validate.phone.Phone;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.personnel.role.RoleEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

/**
 * <h1>用户实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
@Description("用户")
public class UserEntity extends BaseEntity<UserEntity> {
    /**
     * <h2>邮箱(唯一)</h2>
     */
    @Description("账号")
    @Column(columnDefinition = "varchar(255) default '' comment '账号'", unique = true)
    @Search()
    private String account;

    /**
     * <h2>邮箱(唯一)</h2>
     */
    @Description("邮箱")
    @Column(columnDefinition = "varchar(255) default '' comment '邮箱'", unique = true)
    @NotBlank(groups = {WhenSendEmail.class}, message = "邮箱不能为空")
    @Email(groups = {WhenSendEmail.class}, message = "邮箱格式不正确")
    @Search()
    private String email;

    /**
     * <h2>手机(唯一)</h2>
     */
    @Description("手机")
    @Column(columnDefinition = "varchar(255) default '' comment '手机'", unique = true)
    @NotBlank(groups = {WhenSendMessage.class}, message = "手机不能为空")
    @Phone(groups = {WhenSendMessage.class}, message = "手机格式不正确", tel = false)
    @Search()
    private String phone;

    /**
     * <h2>用户的密码(不返回给前端)</h2>
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Description("密码")
    @Column(columnDefinition = "varchar(255) default '' comment '密码'")
    @NotBlank(groups = {WhenLogin.class}, message = "密码不能为空")
    @Null(groups = {WhenUpdateMyInfo.class}, message = "请勿传入password字段")
    @Password(message = "密码要求6-16位且至少包含大小写字母和数字")
    private String password;

    /**
     * <h2>用户昵称</h2>
     */
    @Column(columnDefinition = "varchar(255) default '' comment '昵称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class, WhenUpdateMyInfo.class}, message = "昵称不能为空")
    @Search()
    private String nickname;

    /**
     * <h2>是否系统用户</h2>
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否系统用户'")
    @Null(groups = {WhenUpdateMyInfo.class}, message = "请勿传入isSystem字段")
    @Search(Search.Mode.EQUALS)
    private Boolean isSystem;

    /**
     * <h2>密码盐</h2>
     */
    @JsonIgnore
    @Column(columnDefinition = "varchar(255) default '' comment '密码盐'")
    private String salt;

    /**
     * <h2>角色列表</h2>
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @Payload
    @Exclude(filters = {WhenPayLoad.class})
    private Set<RoleEntity> roleList;

    /**
     * <h2>登录使用的App秘钥</h2>
     */
    private String appKey;

    /**
     * <h2>邮箱验证码</h2>
     */
    @NotBlank(groups = {WhenLoginViaEmail.class, WhenLoginViaPhone.class}, message = "验证码不能为空")
    private String code;

    /**
     * <h2>修改密码时使用的原始密码</h2>
     */
    @NotBlank(groups = {WhenUpdateMyPassword.class}, message = "原始密码不能为空")
    private String oldPassword;

    /**
     * ID+密码 邮箱+密码登录
     */
    public interface WhenLogin {
    }

    /**
     * 邮箱+验证码登录
     */
    public interface WhenLoginViaEmail {
    }

    /**
     * 手机+验证码登录
     */
    public interface WhenLoginViaPhone {
    }

    public interface WhenUpdateMyPassword {
    }

    public interface WhenUpdateMyInfo {
    }

    public interface WhenSendEmail {
    }

    public interface WhenSendMessage {
    }

    public interface WhenGetMyInfo {
    }
}
