package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Desensitize;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.Dictionary;
import cn.hamm.airpower.validate.Phone;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.enums.UserGender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.Objects;
import java.util.Set;

import static cn.hamm.airpower.annotation.Desensitize.Type.CHINESE_NAME;
import static cn.hamm.airpower.annotation.Desensitize.Type.ID_CARD;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static jakarta.persistence.FetchType.EAGER;

/**
 * <h1>用户实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
@Description("用户")
public class UserEntity extends BaseEntity<UserEntity> implements IUserAction {
    @Description("用户昵称")
    @Column(columnDefinition = "varchar(255) default '' comment '昵称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class, WhenUpdateMyInfo.class}, message = "昵称不能为空")
    @Search
    private String nickname;

    @Description("头像")
    @Column(columnDefinition = "varchar(255) default '' comment '头像'")
    private String avatar;

    @Description("真实姓名")
    @Desensitize(CHINESE_NAME)
    @Column(columnDefinition = "varchar(255) default '' comment '真实姓名'")
    private String realName;

    @Description("身份证号")
    @Desensitize(ID_CARD)
    @Column(columnDefinition = "varchar(255) default '' comment '身份证号'")
    private String idCard;

    @Description("邮箱")
    @Column(columnDefinition = "varchar(255) default '' comment '邮箱'", unique = true)
    @NotBlank(groups = {WhenSendEmail.class}, message = "邮箱不能为空")
    @Email(groups = {WhenResetMyPassword.class, WhenSendEmail.class}, message = "邮箱格式不正确")
    @Search()
    private String email;

    @Description("手机号")
    @Column(columnDefinition = "varchar(255) default '' comment '手机号'", unique = true)
    @Phone(groups = {WhenResetMyPassword.class, WhenSendSms.class}, message = "手机格式不正确")
    @Search()
    private String phone;

    @Description("性别")
    @Dictionary(value = UserGender.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '性别'")
    @Search(Search.Mode.EQUALS)
    private Integer gender;

    @JsonProperty(access = WRITE_ONLY)
    @Description("密码")
    @Column(columnDefinition = "varchar(255) default '' comment '密码'")
    @NotBlank(groups = {WhenLogin.class, WhenResetMyPassword.class, WhenUpdateMyPassword.class}, message = "密码不能为空")
    @Null(groups = {WhenUpdateMyInfo.class}, message = "请勿传入password字段")
    @Length(min = 6, message = "密码至少6位长度")
    private String password;

    @Description("密码盐")
    @JsonIgnore
    @Column(columnDefinition = "varchar(255) default '' comment '密码盐'")
    private String salt;

    @Description("角色列表")
    @ManyToMany(fetch = EAGER)
    private Set<RoleEntity> roleList;

    @Description("部门列表")
    @ManyToMany(fetch = EAGER)
    private Set<DepartmentEntity> departmentList;

    /// ////////////////////

    @Description("邮箱验证码")
    @NotBlank(groups = {WhenResetMyPassword.class}, message = "邮箱验证码不能为空")
    @Transient
    private String code;

    @Description("原始密码")
    @NotBlank(groups = {WhenUpdateMyPassword.class}, message = "原始密码不能为空")
    @Transient
    private String oldPassword;

    @Description("部门ID查询")
    @Transient
    private Long departmentId;

    /**
     * <h3>获取是否超级管理员</h3>
     *
     * @return 结果
     */
    @Transient
    @JsonIgnore
    public final boolean isRootUser() {
        return Objects.nonNull(getId()) && getId() == 1L;
    }

    @Override
    public void excludeBaseData() {
        super.excludeBaseData();
        this.setRealName(null).setIdCard(null).setEmail(null).setPhone(null);
    }
}
