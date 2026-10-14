package cn.hamm.spms.module.open.notify.enums;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>通知渠道</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
@Description("通知渠道")
public enum NotifyChannel implements IDictionary {
    /**
     * 企业微信
     */
    WORK_WECHAT(1, "企业微信"),

    /**
     * 飞书
     */
    FEI_SHU(2, "飞书"),

    /**
     * 钉钉
     */
    DING_TALK(3, "钉钉"),

    /**
     * 邮件
     */
    EMAIL(4, "邮件"),

    /**
     * WebHook
     */
    WEB_HOOK(5, "WebHook");

    private final int key;
    private final String label;
}
