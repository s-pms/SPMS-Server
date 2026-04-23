package cn.hamm.spms.module.chat;

import cn.hamm.spms.module.chat.member.MemberService;
import cn.hamm.spms.module.chat.room.RoomService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ChatServices {
    @Getter
    private static MemberService memberService;

    @Getter
    private static RoomService roomService;

    @Autowired
    private void initService(
            MemberService memberService,
            RoomService roomService
    ) {
        ChatServices.memberService = memberService;
        ChatServices.roomService = roomService;
    }
}
