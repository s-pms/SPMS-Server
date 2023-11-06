package cn.hamm.starter.module.system.user;

import cn.hamm.airpower.annotation.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * <h1>Vo</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@Description("用户")
public class UserVo extends UserEntity {
    /**
     * <h2>登录使用的App秘钥</h2>
     */
    private String appKey;

    /**
     * <h2>邮箱验证码</h2>
     */
    @NotBlank(groups = {WhenRegister.class, WhenUpdateMyPassword.class, WhenResetMyPassword.class}, message = "邮箱验证码不能为空")
    private String code;

    /**
     * <h2>修改密码时使用的原始密码</h2>
     */
    @NotBlank(groups = {WhenUpdateMyPassword.class}, message = "原始密码不能为空")
    private String oldPassword;
}
