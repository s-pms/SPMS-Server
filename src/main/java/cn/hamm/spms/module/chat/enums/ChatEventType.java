package cn.hamm.spms.module.chat.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * <h1>聊天事件类型</h1>
 *
 * @author Hamm
 * @apiNote {@code key} 即 WebSocket 报文里的 {@code type} 字段，客户端上行和服务端下行共用同一套编码：
 * {@link #ROOM_MEMBER_JOIN}、{@link #ROOM_MEMBER_LEAVE}、{@link #ROOM_TEXT_MESSAGE} 三个值是双向的，
 * 其余只由服务端下发。数值约定：{@code 0-99} 为通用状态、{@code 1000-1099} 为房间进出房、
 * {@code 1100+} 为房间内消息
 */
@Getter
@AllArgsConstructor
public enum ChatEventType implements IDictionary {
    /**
     * 未知：{@code key} 为空或解析不出任何枚举项时的兜底值
     */
    UNKNOWN(0, "未知"),

    /**
     * 上线：用户建立 WebSocket 连接
     */
    ONLINE(1, "上线"),

    /**
     * 下线：用户 WebSocket 连接断开
     */
    OFFLINE(2, "下线"),

    /**
     * 加入房间：客户端上行请求进房，服务端校验通过后以同一 {@code type} 向房间频道广播该成员已进房
     */
    ROOM_MEMBER_JOIN(1001, "加入房间"),

    /**
     * 离开房间：客户端上行请求离房，或连接断开时服务端自动离房，同样以该 {@code type} 广播
     */
    ROOM_MEMBER_LEAVE(1002, "离开房间"),

    /**
     * 加入房间失败：房间号不存在、未提供房间号或密码错误，仅单播回请求方
     */
    ROOM_JOIN_FAIL(1003, "加入房间失败"),

    /**
     * 加入房间成功：仅单播回请求方，不广播
     */
    ROOM_JOIN_SUCCESS(1004, "加入房间成功"),

    /**
     * 离开房间成功：仅单播回请求方，不广播
     */
    ROOM_LEAVE_SUCCESS(1005, "离开房间成功"),

    /**
     * 离开发房间失败：预留，当前离房流程不会失败，服务端未下发过该事件
     */
    ROOM_LEAVE_FAIL(1006, "离开房间失败"),

    /**
     * 在线人数变更：每次进出房后紧随广播，{@code data} 为该房间最新的在线用户 ID 列表
     */
    ONLINE_COUNT_CHANGED(1050, "在线人数变更"),

    /**
     * 房间文本消息：客户端上行发送文本，服务端以同一 {@code type} 广播给房间内所有人
     */
    ROOM_TEXT_MESSAGE(1100, "房间文本消息"),

    ;

    private final int key;
    private final String label;

    /**
     * 通过字符串变量获取枚举项
     *
     * @param key 事件 {@code type} 的字符串形式
     * @return 匹配的枚举项，解析不到时返回 {@link #UNKNOWN}
     * @apiNote 传 {@code null} 走兜底，但非数字字符串会在 {@code Integer.parseInt} 处抛异常
     */
    public static ChatEventType getByStringKey(String key) {
        if (Objects.isNull(key)) {
            return UNKNOWN;
        }
        for (ChatEventType item : values()) {
            if (item.getKey() == Integer.parseInt(key)) {
                return item;
            }
        }
        return UNKNOWN;
    }

    /**
     * 获取字符串类型的 Key
     *
     * @return 字符串类型的 Key
     */
    @Contract(pure = true)
    public @NotNull String getKeyString() {
        return String.valueOf(key);
    }
}
