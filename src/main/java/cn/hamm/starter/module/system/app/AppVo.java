package cn.hamm.starter.module.system.app;

import cn.hamm.airpower.annotation.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

/**
 * <h1>AppVo</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppVo extends AppEntity {
    /**
     * <h2>临时code</h2>
     */
    @Description("临时码")
    @NotBlank(groups = {AppEntity.WhenCode2AccessToken.class})
    @NotBlank(groups = {AppEntity.WhenAccessToken.class}, message = "Code不能为空")
    @Transient
    private String code;

    /**
     * <h2>Cookie</h2>
     */
    @Transient
    private String cookie;
}
