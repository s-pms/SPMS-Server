package cn.hamm.spms.module.chat.room.model;

import cn.hamm.airpower.root.RootModel;
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
     * 密码
     */
    private String password;
}
