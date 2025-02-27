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
     * <h2>超管</h2>
     */
    ADMIN(1, "超管"),

    /**
     * <h2>房主</h2>
     */
    OWNER(2, "房主"),

    /**
     * <h2>副房主</h2>
     */
    ASSISTANT(3, "副房主"),

    /**
     * <h2>管理员</h2>
     */
    MANAGER(4, "管理员"),

    /**
     * <h2>成员</h2>
     */
    MEMBER(5, "成员"),

    /**
     * <h2>游客</h2>
     */
    VISITOR(6, "游客"),
    ;
    private final int key;
    private final String label;
}
