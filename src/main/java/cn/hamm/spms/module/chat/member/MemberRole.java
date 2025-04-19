package cn.hamm.spms.module.chat.member;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>成员类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
@Description("成员类型")
public enum MemberRole implements IDictionary {
    /**
     * 超管
     */
    ADMIN(1, "超管"),

    /**
     * 房主
     */
    OWNER(2, "房主"),

    /**
     * 副房主
     */
    ASSISTANT(3, "副房主"),

    /**
     * 管理员
     */
    MANAGER(4, "管理员"),

    /**
     * 成员
     */
    MEMBER(5, "成员"),

    /**
     * 游客
     */
    VISITOR(6, "游客"),
    ;
    private final int key;
    private final String label;
}
