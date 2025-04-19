package cn.hamm.spms.common;

import cn.hamm.airpower.helper.WebsocketHelper;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.websocket.WebSocketHandler;
import cn.hamm.airpower.websocket.WebSocketPayload;
import cn.hamm.spms.module.chat.enums.ChatEventType;
import cn.hamm.spms.module.chat.member.MemberEntity;
import cn.hamm.spms.module.chat.member.MemberService;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.chat.room.RoomService;
import cn.hamm.spms.module.chat.room.event.MemberTextMessageEvent;
import cn.hamm.spms.module.chat.room.model.RoomJoinRequest;
import cn.hamm.spms.module.chat.room.model.RoomMemberEvent;
import cn.hamm.spms.module.personnel.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hamm.airpower.exception.ServiceError.PARAM_INVALID;
import static cn.hamm.spms.module.chat.enums.ChatEventType.*;

/**
 * <h1>应用自定义的事件处理器</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Component
public class AppWebSocketHandler extends WebSocketHandler {
    /**
     * 订阅分组前缀
     */
    private static final String GROUP_PREFIX = "group_";

    /**
     * 房间在线用户列表
     */
    protected final ConcurrentHashMap<String, List<Long>> roomOnlineUserList = new ConcurrentHashMap<>();

    @Autowired
    private WebsocketHelper websocketHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RoomService roomService;

    /**
     * 房间事件
     *
     * @param userId 用户ID
     * @param roomId 房间ID
     * @param event  事件类型
     */
    private void onRoomEvent(long userId, long roomId, @NotNull ChatEventType event) {
        List<Long> roomUserIdList = roomOnlineUserList.get(GROUP_PREFIX + roomId);
        if (Objects.isNull(roomUserIdList)) {
            roomUserIdList = new ArrayList<>();
        }
        switch (event) {
            case ROOM_MEMBER_JOIN:
                if (!roomUserIdList.contains(userId)) {
                    roomUserIdList.add(userId);
                }
                break;
            case ROOM_MEMBER_LEAVE:
                roomUserIdList.remove(userId);
                break;
            default:
                PARAM_INVALID.show("错误的房间事件异常类型");
        }
        roomOnlineUserList.put(GROUP_PREFIX + roomId, roomUserIdList);
        MemberEntity member = memberService.getMemberWithAutoCreate(userId, roomId);
        member.getUser().setEmail(null).excludeBaseData();
        member.getRoom().setPassword(null).excludeBaseData();

        RoomMemberEvent roomMemberEvent = new RoomMemberEvent();
        roomMemberEvent.setMember(member);

        websocketHelper.publishToChannel(GROUP_PREFIX + roomId, new WebSocketPayload()
                .setType(event.getKeyString())
                .setData(Json.toString(roomMemberEvent)));
        websocketHelper.publishToChannel(GROUP_PREFIX + roomId, new WebSocketPayload()
                .setType(ONLINE_COUNT_CHANGED.getKeyString())
                .setData(Json.toString(roomUserIdList))
        );
    }

    @Override
    protected void onWebSocketPayload(@NotNull WebSocketPayload webSocketPayload, @NotNull WebSocketSession session) {
        Long userId = userIdHashMap.get(session.getId());
        if (Objects.isNull(userId)) {
            return;
        }
        switch (getByStringKey(webSocketPayload.getType())) {
            case ROOM_MEMBER_JOIN:
                RoomJoinRequest joinRequest = Json.parse(webSocketPayload.getData(), RoomJoinRequest.class);
                // 查房间信息
                RoomEntity room = roomService.getByCode(joinRequest.getRoomCode());
                if (Objects.isNull(room)) {
                    webSocketPayload = new WebSocketPayload()
                            .setType(ROOM_JOIN_FAIL.getKeyString())
                            .setData("房间号 " + joinRequest.getRoomCode() + "不存在");
                    sendWebSocketPayload(session, webSocketPayload);
                    return;
                }
                MemberEntity joinMember = memberService.getMemberWithAutoCreate(userId, room.getId());
                if (roomService.checkIfNeedPassword(joinMember) &&
                        !room.getPassword().equalsIgnoreCase(joinRequest.getPassword())) {
                    // todo 密码不正确
                    webSocketPayload = new WebSocketPayload()
                            .setType(ROOM_JOIN_FAIL.getKeyString())
                            .setData("进入房间失败，房间密码错误！");
                    sendWebSocketPayload(session, webSocketPayload);
                    return;
                }

                // 更新用户当前所在房间ID到缓存
                userService.saveCurrentRoomId(userId, room.getId());
                onRoomEvent(userId, room.getId(), ROOM_MEMBER_JOIN);
                subscribe(GROUP_PREFIX + room.getId(), session);

                RoomMemberEvent memberJoinEvent = new RoomMemberEvent();
                memberJoinEvent.setMember(getCurrentMember(userId));
                sendWebSocketPayload(session, new WebSocketPayload()
                        .setType(ROOM_JOIN_SUCCESS.getKeyString())
                        .setData(Json.toString(memberJoinEvent)));

                List<Long> roomUserIdList = roomOnlineUserList.get(GROUP_PREFIX + room.getId());
                sendWebSocketPayload(session, new WebSocketPayload()
                        .setType(ONLINE_COUNT_CHANGED.getKeyString())
                        .setData(Json.toString(roomUserIdList)));
                break;
            case ROOM_MEMBER_LEAVE:
                leaveRoom(session, userId);
                break;
            case ROOM_TEXT_MESSAGE:
                MemberTextMessageEvent memberTextMessageEvent = new MemberTextMessageEvent();
                memberTextMessageEvent.setText(webSocketPayload.getData()).setMember(getCurrentMember(userId));
                publishToUserRoom(userId, ROOM_TEXT_MESSAGE, memberTextMessageEvent);
                break;
            default:
        }
    }

    /**
     * 离开房间
     *
     * @param session websocket会话
     * @param userId  用户ID
     */
    private void leaveRoom(@NotNull WebSocketSession session, long userId) {
        long leaveRoomId = userService.getCurrentRoomId(userId);
        onRoomEvent(userId, leaveRoomId, ROOM_MEMBER_LEAVE);
        unsubscribe(GROUP_PREFIX + leaveRoomId, session);

        sendWebSocketPayload(session, new WebSocketPayload()
                .setType(ROOM_LEAVE_SUCCESS.getKeyString())
        );
    }

    /**
     * 发布消息到当前用户的房间
     *
     * @param userId 用户ID
     * @param type   世界事件类型
     * @param event  事件
     */
    private void publishToUserRoom(long userId, @NotNull ChatEventType type, RoomMemberEvent event) {
        WebSocketPayload payload = new WebSocketPayload();
        payload.setType(type.getKeyString()).setData(Json.toString(event));
        websocketHelper.publishToChannel(GROUP_PREFIX + userService.getCurrentRoomId(userId), payload);
    }

    /**
     * 获取当前用户的当前房间的成员信息
     *
     * @param userId 用户ID
     * @return 成员信息
     */
    private @NotNull MemberEntity getCurrentMember(long userId) {
        long roomId = userService.getCurrentRoomId(userId);
        MemberEntity member = memberService.getMemberWithAutoCreate(userId, roomId);
        member.excludeBaseData();
        return member;
    }

    @Override
    protected void afterDisconnect(@NotNull WebSocketSession session, Long userId) {
        if (Objects.nonNull(userId)) {
            leaveRoom(session, userId);
        }
    }
}
