package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>普通令牌</h3>
     */
    NORMAL(0, "普通令牌"),

    /**
     * <h3>私人令牌</h3>
     */
    PERSONAL(1, "私人令牌"),

    /**
     * <h3>第三方令牌</h3>
     */
    OAUTH2(2, "第三方令牌"),
    ;
    /**
     * <h3>用户令牌类型</h3>
     */
    public static final String USER_TOKEN_TYPE = "type";
    private final int key;
    private final String label;
}
