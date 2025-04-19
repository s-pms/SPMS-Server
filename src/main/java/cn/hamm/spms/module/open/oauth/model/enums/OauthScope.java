package cn.hamm.spms.module.open.oauth.model.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>授权范围</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum OauthScope implements IDictionary {
    /**
     * 基础信息
     */
    BASIC_INFO(1, "基础信息", true, "ID/昵称/头像等基础信息"),

    /**
     * 隐私信息
     */
    PRIVACY(2, "隐私信息", false, "生日/性别/注册时间等隐私信息"),

    /**
     * 联系方式
     */
    CONTACT(3, "联系方式", false, "手机号/邮箱等联系方式"),

    /**
     * 实名信息
     */
    REAL_NAME(4, "实名信息", false, "真实姓名/身份证号等实名信息"),

    ;
    private final int key;
    private final String label;
    private final Boolean isDefault;
    private final String description;
}
