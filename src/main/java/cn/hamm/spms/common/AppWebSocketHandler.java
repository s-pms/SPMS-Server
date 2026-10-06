package cn.hamm.spms.common;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.websocket.WebSocketHandler;
import cn.hamm.airpower.websocket.WebSocketHelper;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;
import static cn.hamm.spms.module.chat.enums.ChatEventType.*;

/**
 * <h1>房间聊天事件处理</h1>
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
     * 各房间的在线用户 ID 集合，key 为 {@code GROUP_PREFIX} + 房间 ID
     * <p>
     * 值必须是并发集合：{@code ConcurrentHashMap} 只保证映射本身的操作原子，
     * 换成 {@code List} 后同一房间并发进出时 {@code contains}/{@code add} 会丢记录，
     * 广播出去的在线人数随之失真。
     * </p>
     */
    protected final ConcurrentHashMap<String, Set<Long>> roomOnlineUserSet = new ConcurrentHashMap<>();

    @Autowired
    private WebSocketHelper webSocketHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RoomService roomService;

    /**
     * 房间事件
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @param event  事件类型
     */
    private void onRoomEvent(long userId, long roomId, @NotNull ChatEventType event) {
        String groupKey = GROUP_PREFIX + roomId;
        // computeIfAbsent 保证「取集合」与「建集合」是同一次原子操作，
        // 避免两个线程各自 new 出一个集合后互相覆盖
        Set<Long> onlineUsers = roomOnlineUserSet.computeIfAbsent(groupKey, key -> ConcurrentHashMap.newKeySet());
        switch (event) {
            case ROOM_MEMBER_JOIN -> onlineUsers.add(userId);
            case ROOM_MEMBER_LEAVE -> onlineUsers.remove(userId);
            default -> PARAM_INVALID.show("错误的房间事件异常类型");
        }
        if (onlineUsers.isEmpty()) {
            roomOnlineUserSet.remove(groupKey, onlineUsers);
        }
        MemberEntity member = memberService.getMemberWithAutoCreate(userId, roomId);
        member.getUser().excludeNotMeta();
        member.getRoom().excludeNotMeta();

        RoomMemberEvent roomMemberEvent = new RoomMemberEvent();
        roomMemberEvent.setMember(member);

        webSocketHelper.publishToChannel(groupKey, new WebSocketPayload()
                .setType(event.getKeyString())
                .setData(Json.toString(roomMemberEvent)));
        webSocketHelper.publishToChannel(groupKey, new WebSocketPayload()
                .setType(ONLINE_COUNT_CHANGED.getKeyString())
                .setData(Json.toString(List.copyOf(onlineUsers)))
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
                // 客户端可能发 {"type":"room_member_join","data":"{}"}，roomCode 为 null 时拆箱会抛 NPE，
                // 异常冒到 handleTextMessage 后这条 WS 连接后续消息全部失效
                if (Objects.isNull(joinRequest) || Objects.isNull(joinRequest.getRoomCode())) {
                    sendWebSocketPayload(session, new WebSocketPayload()
                            .setType(ROOM_JOIN_FAIL.getKeyString())
                            .setData("请提供正确的房间号"));
                    return;
                }
                RoomEntity room = roomService.getByCode(joinRequest.getRoomCode());
                if (Objects.isNull(room)) {
                    webSocketPayload = new WebSocketPayload()
                            .setType(ROOM_JOIN_FAIL.getKeyString())
                            .setData("房间号 " + joinRequest.getRoomCode() + "不存在");
                    sendWebSocketPayload(session, webSocketPayload);
                    return;
                }
                // 鉴权必须排在落库之前：getMemberWithAutoCreate 会真的 addAndGet，
                // 顺序反了密码输错也能写入成员记录，6 位房间号（约 90 万种）足够批量污染成员表
                if (isRoomJoinRejected(userId, room, joinRequest)) {
                    sendWebSocketPayload(session, new WebSocketPayload()
                            .setType(ROOM_JOIN_FAIL.getKeyString())
                            .setData("进入房间失败，房间密码错误！"));
                    return;
                }
                MemberEntity joinMember = memberService.getMemberWithAutoCreate(userId, room.getId());

                userService.saveCurrentRoomId(userId, room.getId());
                onRoomEvent(userId, room.getId(), ROOM_MEMBER_JOIN);
                subscribe(GROUP_PREFIX + room.getId(), session);

                RoomMemberEvent memberJoinEvent = new RoomMemberEvent();
                memberJoinEvent.setMember(getCurrentMember(userId));
                sendWebSocketPayload(session, new WebSocketPayload()
                        .setType(ROOM_JOIN_SUCCESS.getKeyString())
                        .setData(Json.toString(memberJoinEvent)));

                Set<Long> onlineUsers = roomOnlineUserSet.get(GROUP_PREFIX + room.getId());
                sendWebSocketPayload(session, new WebSocketPayload()
                        .setType(ONLINE_COUNT_CHANGED.getKeyString())
                        .setData(Json.toString(List.copyOf(Objects.requireNonNullElse(onlineUsers, Set.of())))));
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
     * 判断加入房间的请求是否应被拒绝（仅密码校验）
     *
     * @param userId      用户 ID
     * @param room        房间
     * @param joinRequest 加房请求
     * @return true 表示应拒绝
     * @apiNote 必须先于任何写库动作调用：{@code getMemberWithAutoCreate} 会真的落库，
     * 放到密码校验之后会让密码输错也能写入成员记录。密码比较用
     * {@code MessageDigest.isEqual} 而非 {@code equalsIgnoreCase}，后者不是恒定时间比较，
     * 会通过响应耗时侧信道逐字符试探密码
     */
    private boolean isRoomJoinRejected(long userId, @NotNull RoomEntity room, @NotNull RoomJoinRequest joinRequest) {
        // 非私有房间不需要密码
        if (!Boolean.TRUE.equals(room.getIsPrivate())) {
            return false;
        }
        // 房主与管理员不需要密码
        MemberEntity exist = memberService.getMember(userId, room.getId());
        if (Objects.nonNull(exist) && !roomService.checkIfNeedPassword(exist)) {
            return false;
        }
        String expect = Objects.toString(room.getPassword(), "");
        String input = Objects.toString(joinRequest.getPassword(), "");
        return !MessageDigest.isEqual(
                expect.getBytes(StandardCharsets.UTF_8),
                input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 离开房间
     *
     * @param session Websocket 会话
     * @param userId  用户 ID
     */
    private void leaveRoom(@NotNull WebSocketSession session, long userId) {
        long leaveRoomId = userService.getCurrentRoomId(userId);
        // 不能拿 getCurrentRoomId 的返回值来判断：Redis 无缓存时它返回默认房间 ID（1），
        // 于是任何用户断开连接都会向默认房间广播一次「成员离开」，污染其在线人数统计
        if (!userService.isInRoom(userId, leaveRoomId)) {
            log.debug("用户 {} 当前不在任何房间，忽略离开事件", userId);
            return;
        }
        onRoomEvent(userId, leaveRoomId, ROOM_MEMBER_LEAVE);
        unsubscribe(GROUP_PREFIX + leaveRoomId, session);
        userService.clearCurrentRoomId(userId);

        sendWebSocketPayload(session, new WebSocketPayload()
                .setType(ROOM_LEAVE_SUCCESS.getKeyString())
        );
    }

    /**
     * 发布消息到当前用户的房间
     *
     * @param userId 用户 ID
     * @param type   世界事件类型
     * @param event  事件
     */
    private void publishToUserRoom(long userId, @NotNull ChatEventType type, RoomMemberEvent event) {
        WebSocketPayload payload = new WebSocketPayload();
        payload.setType(type.getKeyString()).setData(Json.toString(event));
        webSocketHelper.publishToChannel(GROUP_PREFIX + userService.getCurrentRoomId(userId), payload);
    }

    /**
     * 获取当前用户的当前房间的成员信息
     *
     * @param userId 用户 ID
     * @return 成员信息
     */
    private @NotNull MemberEntity getCurrentMember(long userId) {
        long roomId = userService.getCurrentRoomId(userId);
        MemberEntity member = memberService.getMemberWithAutoCreate(userId, roomId);
        member.excludeNotMeta();
        return member;
    }

    @Override
    protected void afterDisconnect(@NotNull WebSocketSession session, Long userId) {
        if (Objects.nonNull(userId)) {
            leaveRoom(session, userId);
        }
    }
}
