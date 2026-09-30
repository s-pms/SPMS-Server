package cn.hamm.spms.module.chat.member.enums;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>成员类型</h1>
 *
 * @author Hamm.cn
 * @apiNote 仅 {@link #OWNER} 与 {@link #VISITOR} 会被 {@code MemberService.addMember} 自动赋值：
 * 房主本人记为 {@code OWNER}，其他人一律记为 {@code VISITOR}，其余枚举值需手工改
 */
@AllArgsConstructor
@Getter
@Description("成员类型")
public enum MemberRole implements IDictionary {
    /**
     * 超管：平台侧管理员，权限最大
     */
    ADMIN(1, "超管"),

    /**
     * 房主：房间所有者，只能是房间创建人
     */
    OWNER(2, "房主"),

    /**
     * 副房主：房主的协管者
     */
    ASSISTANT(3, "副房主"),

    /**
     * 管理员：房间内的日常管理
     */
    MANAGER(4, "管理员"),

    /**
     * 成员：正式成员
     */
    MEMBER(5, "成员"),

    /**
     * 游客：新进房用户的默认角色
     */
    VISITOR(6, "游客"),
    ;
    private final int key;
    private final String label;
}
