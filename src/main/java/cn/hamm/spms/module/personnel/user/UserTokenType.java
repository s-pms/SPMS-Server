package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>用户令牌类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum UserTokenType implements IDictionary {
    /**
     * 普通令牌
     */
    NORMAL(0, "普通令牌"),

    /**
     * 私人令牌
     */
    PERSONAL(1, "私人令牌"),

    /**
     * 第三方令牌
     */
    OAUTH2(2, "第三方令牌"),
    ;
    /**
     * 用户令牌类型
     */
    public static final String TYPE = "type";
    private final int key;
    private final String label;
}
