package cn.hamm.spms.module.chat.room.model;

import cn.hamm.spms.module.chat.event.ChatEvent;
import cn.hamm.spms.module.chat.member.MemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>房间成员事件</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RoomMemberEvent extends ChatEvent {
    /**
     * 成员信息，仅返回 {@code @Meta} 字段，避免把用户敏感信息推给房间内所有人
     */
    private MemberEntity member;
}
