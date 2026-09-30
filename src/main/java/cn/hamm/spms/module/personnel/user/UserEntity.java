package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.core.annotation.*;
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
    // 唯一索引列不能用 default ''：@DynamicInsert 会省略 null 字段，MySQL 随即填入 ''，
    // 导致全库只能存在一个"没有邮箱的用户"，第二个必然撞唯一索引。
    // 改为 default null 后，MySQL 唯一索引允许多个 NULL。
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

    /// ////////////////////

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
     * <p>
     * 关联关系由 {@code user_role_link} 中间表承载（见 {@code UserRoleLinkEntity}），
     * 本字段仅用于承接前端提交的 JSON 与回传读取结果，不参与持久化。
     * 读取由 {@code UserService} 组装（并级联组装每个角色的菜单与权限，
     * 因为 RBAC 鉴权链路 {@code RequestInterceptor} 会一路读到
     * {@code role.getPermissionList()}），写入由其同步中间表。
     * </p>
     */
    @Description("角色列表")
    @Transient
    private Set<RoleEntity> roleList;

    /**
     * 用户所属的部门
     * <p>
     * 仍保留 {@code @ManyToMany}：{@code UserService.addSearchPredicate} 用
     * {@code root.join("departmentList")} 做按部门筛选的 Criteria 查询，
     * 改为中间表实体需要同步重写该查询。已在 P2 报告中记录为待改造项。
     * </p>
     */
    @Description("部门列表")
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<DepartmentEntity> departmentList;

    /**
     * 获取是否超级管理员
     *
     * @return 结果
     */
    @Transient
    @JsonIgnore
    public final boolean isRootUser() {
        return Objects.nonNull(getId()) && getId() == 1L;
    }
}
