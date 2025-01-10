package cn.hamm.spms.module.personnel.user.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>性别枚举</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum UserGender implements IDictionary {
    /**
     * <h3>女</h3>
     */
    FEMALE(0, "女"),

    /**
     * <h3>男</h3>
     */
    MALE(1, "男"),

    ;
    private final int key;
    private final String label;
}
