package cn.hamm.spms.module.chat.room.model;

import cn.hamm.airpower.core.model.RootModel;
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
     * <h3>房间号</h3>
     */
    private Integer roomCode;

    /**
     * <h3>密码</h3>
     */
    private String password;
}
