package cn.hamm.spms.module.chat.room;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>Repository</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface RoomRepository extends BaseRepository<RoomEntity> {
    /**
     * 根据房间代码获取房间信息
     *
     * @param code 房间代码
     * @return 房间信息
     */
    RoomEntity getByCode(Integer code);
}
