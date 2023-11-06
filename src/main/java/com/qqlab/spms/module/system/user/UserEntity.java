package com.qqlab.spms.module.system.user;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.password.Password;
import com.qqlab.spms.base.BaseEntity;
import com.qqlab.spms.module.system.role.RoleEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
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
    @Description("邮箱")
    @Column(columnDefinition = "varchar(255) default '' comment '邮箱'", unique = true)
    @NotBlank(groups = {WhenRegister.class, WhenResetMyPassword.class, WhenSendEmail.class, WhenAdd.class}, message = "邮箱不能为空")
    @Email(groups = {WhenRegister.class, WhenResetMyPassword.class, WhenSendEmail.class}, message = "邮箱格式不正确")
    @Null(groups = {WhenUpdateMyInfo.class}, message = "请勿传入email字段")
    @Search()
    private String email;

    /**
     * <h2>用户的密码(不返回给前端)</h2>
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Description("密码")
    @Column(columnDefinition = "varchar(255) default '' comment '密码'")
    @NotBlank(groups = {WhenLogin.class, WhenRegister.class, WhenResetMyPassword.class}, message = "密码不能为空")
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
     * ID+密码 邮箱+密码登录
     */
    public interface WhenLogin {
    }

    /**
     * 邮箱+验证码登录
     */
    public interface WhenLoginViaEmail {
    }

    public interface WhenRegister {
    }

    public interface WhenResetMyPassword {
    }

    public interface WhenUpdateMyPassword {
    }

    public interface WhenUpdateMyInfo {
    }

    public interface WhenSendEmail {
    }

    public interface WhenGetMyInfo {
    }
}
