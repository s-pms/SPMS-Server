package cn.hamm.spms.module.chat.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * <h1>聊天事件类型</h1>
 *
 * @author Hamm
 */
@Getter
@AllArgsConstructor
public enum ChatEventType implements IDictionary {
    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 上线
     */
    ONLINE(1, "上线"),

    /**
     * 下线
     */
    OFFLINE(2, "下线"),

    /**
     * 加入房间
     */
    ROOM_MEMBER_JOIN(1001, "加入房间"),

    /**
     * 房间离开房间
     */
    ROOM_MEMBER_LEAVE(1002, "离开房间"),

    /**
     * 加入房间失败
     */
    ROOM_JOIN_FAIL(1003, "加入房间失败"),

    /**
     * 加入房间成功
     */
    ROOM_JOIN_SUCCESS(1004, "加入房间成功"),

    /**
     * 离开房间成功
     */
    ROOM_LEAVE_SUCCESS(1005, "离开房间成功"),

    /**
     * 离开发房间失败
     */
    ROOM_LEAVE_FAIL(1006, "离开房间失败"),

    /**
     * 在线人数变更
     */
    ONLINE_COUNT_CHANGED(1050, "在线人数变更"),

    /**
     * 房间文本消息
     */
    ROOM_TEXT_MESSAGE(1100, "房间文本消息"),

    ;

    private final int key;
    private final String label;

    /**
     * 通过字符串变量获取枚举项
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
