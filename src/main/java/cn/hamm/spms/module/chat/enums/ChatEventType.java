package cn.hamm.spms.module.chat.enums;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>未知</h3>
     */
    UNKNOWN(0, "未知"),

    /**
     * <h3>上线</h3>
     */
    ONLINE(1, "上线"),

    /**
     * <h3>下线</h3>
     */
    OFFLINE(2, "下线"),

    /**
     * <h3>加入房间</h3>
     */
    ROOM_MEMBER_JOIN(1001, "加入房间"),

    /**
     * <h3>房间离开房间</h3>
     */
    ROOM_MEMBER_LEAVE(1002, "离开房间"),

    /**
     * <h3>加入房间失败</h3>
     */
    ROOM_JOIN_FAIL(1003, "加入房间失败"),

    /**
     * <h3>加入房间成功</h3>
     */
    ROOM_JOIN_SUCCESS(1004, "加入房间成功"),

    /**
     * <h3>离开房间成功</h3>
     */
    ROOM_LEAVE_SUCCESS(1005, "离开房间成功"),

    /**
     * <h3>离开发房间失败</h3>
     */
    ROOM_LEAVE_FAIL(1006, "离开房间失败"),

    /**
     * <h3>在线人数变更</h3>
     */
    ONLINE_COUNT_CHANGED(1050, "在线人数变更"),

    /**
     * <h3>房间文本消息</h3>
     */
    ROOM_TEXT_MESSAGE(1100, "房间文本消息"),

    ;

    private final int key;
    private final String label;

    /**
     * <h3>通过字符串变量获取枚举项</h3>
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
     * <h3>获取字符串类型的Key</h3>
     *
     * @return 字符串类型的Key
     */
    @Contract(pure = true)
    public @NotNull String getKeyString() {
        return String.valueOf(key);
    }
}
