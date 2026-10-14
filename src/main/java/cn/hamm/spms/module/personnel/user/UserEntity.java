package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Desensitize;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.enums.DesensitizeType;
import cn.hamm.airpower.curd.annotation.Search;
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

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

/**
 * <h1>用户</h1>
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
    @Meta
    private String nickname;

    @Description("头像")
    @Column(columnDefinition = "varchar(255) default '' comment '头像'")
    private String avatar;

    @Description("真实姓名")
    @Desensitize(DesensitizeType.CHINESE_NAME)
    @Column(columnDefinition = "varchar(255) default '' comment '真实姓名'")
    private String realName;

    @Description("身份证号")
    @Desensitize(DesensitizeType.ID_CARD)
    @Column(columnDefinition = "varchar(255) default '' comment '身份证号'")
    private String idCard;

    @Description("邮箱")
    // 唯一索引列必须 default null 而不是 ''：@DynamicInsert 会省略 null 字段，MySQL 随即填入 ''，
    // 那样全库只能存在一个「没有邮箱的用户」，第二个必然撞唯一索引。MySQL 唯一索引才允许多个 NULL
    @Column(columnDefinition = "varchar(255) default null comment '邮箱'", unique = true)
    @NotBlank(groups = {WhenSendEmail.class}, message = "邮箱不能为空")
    @Email(groups = {WhenResetMyPassword.class, WhenSendEmail.class}, message = "邮箱格式不正确")
    @Search
    private String email;

    @Description("性别")
    @Dictionary(value = UserGender.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "int UNSIGNED default 0 comment '性别'")
    private Integer gender;

    @JsonProperty(access = WRITE_ONLY)
    @Description("密码")
    @Column(columnDefinition = "varchar(255) default '' comment '密码'")
    @NotBlank(groups = {WhenLogin.class, WhenResetMyPassword.class, WhenUpdateMyPassword.class}, message = "密码不能为空")
    @Null(groups = {WhenUpdateMyInfo.class}, message = "请勿传入 Password 字段")
    @Length(min = 6, message = "密码至少6位长度")
    private String password;

    @Description("密码盐")
    @JsonIgnore
    @Column(columnDefinition = "varchar(255) default '' comment '密码盐'")
    private String salt;

    @Description("邮箱验证码")
    @NotBlank(groups = {WhenResetMyPassword.class}, message = "邮箱验证码不能为空")
    @Transient
    private String code;

    @Description("原始密码")
    @NotBlank(groups = {WhenUpdateMyPassword.class}, message = "原始密码不能为空")
    @Transient
    private String oldPassword;

    @Description("部门 ID 查询")
    @Transient
    private Long departmentId;

    /**
     * 用户的角色
     *
     * @apiNote 关联关系由 {@code user_role_link} 中间表承载，本字段只用于承接前端提交的 JSON
     * 与回传读取结果，不参与持久化；读写都由 {@code UserService} 走中间表服务同步。
     * 不直接用 {@code @ManyToMany} 是因为 RBAC 鉴权需要在事务外读角色的权限，
     * 而 {@code @ManyToMany} 的懒加载在 open-in-view 关闭时不可用
     */
    @Description("角色列表")
    @Transient
    private Set<RoleEntity> roleList;

    /**
     * 用户所属的部门
     *
     * @apiNote 这里仍保留 {@code @ManyToMany}：{@code UserService.addSearchPredicate} 用
     * {@code root.join("departmentList")} 做按部门筛选的 Criteria 查询，
     * 改成中间表实体就必须同步重写那条查询
     */
    @Description("部门列表")
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<DepartmentEntity> departmentList;

    /**
     * 是否超级管理员
     *
     * @return 主用户（{@code id == 1}）返回 {@code true}
     * @apiNote 超级管理员跳过所有权限校验，判断依据是固定 ID，与角色无关
     */
    @Transient
    @JsonIgnore
    public final boolean isRootUser() {
        return Objects.nonNull(getId()) && getId() == 1L;
    }
}
