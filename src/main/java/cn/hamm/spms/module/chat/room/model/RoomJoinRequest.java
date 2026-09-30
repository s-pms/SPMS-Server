package cn.hamm.spms.module.chat.room.model;

import cn.hamm.airpower.core.RootModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>进房申请</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RoomJoinRequest extends RootModel<RoomJoinRequest> {
    /**
     * 房间号
     */
    private Integer roomCode;

    /**
     * 进房密码，仅私有房间需要，服务端以恒定时间比较
     */
    private String password;
}
