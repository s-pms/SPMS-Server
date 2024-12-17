package cn.hamm.spms.module.webhook.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>通知类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum WebHookType implements IDictionary {
    /**
     * <h3>企业微信</h3>
     */
    WORK_WECHAT(1, "企业微信"),

    /**
     * <h3>飞书</h3>
     */
    FEI_SHU(2, "飞书"),

    /**
     * <h3>钉钉</h3>
     */
    DING_TALK(3, "钉钉"),

    /**
     * <h3>邮件</h3>
     */
    EMAIL(4, "邮件"),

    /**
     * <h3>WebHook</h3>
     */
    WEB_HOOK(5, "WebHook");

    private final int key;
    private final String label;
}
