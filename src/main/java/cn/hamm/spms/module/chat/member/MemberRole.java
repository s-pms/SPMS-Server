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
     * <h3>超管</h3>
     */
    ADMIN(1, "超管"),

    /**
     * <h3>房主</h3>
     */
    OWNER(2, "房主"),

    /**
     * <h3>副房主</h3>
     */
    ASSISTANT(3, "副房主"),

    /**
     * <h3>管理员</h3>
     */
    MANAGER(4, "管理员"),

    /**
     * <h3>成员</h3>
     */
    MEMBER(5, "成员"),

    /**
     * <h3>游客</h3>
     */
    VISITOR(6, "游客"),
    ;
    private final int key;
    private final String label;
}
