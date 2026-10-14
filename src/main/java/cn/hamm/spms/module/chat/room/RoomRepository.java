package cn.hamm.spms.module.chat.room;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>房间数据源</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface RoomRepository extends BaseRepository<RoomEntity> {
    /**
     * 根据房间号获取房间信息
     *
     * @param code 房间号
     * @return 房间信息，不存在时返回 {@code null}
     */
    RoomEntity getByCode(Integer code);
}
