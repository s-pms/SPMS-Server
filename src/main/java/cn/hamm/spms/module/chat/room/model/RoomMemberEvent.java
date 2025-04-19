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
     * 成员信息
     */
    private MemberEntity member;
}
