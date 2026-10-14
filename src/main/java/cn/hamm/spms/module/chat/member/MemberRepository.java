package cn.hamm.spms.module.chat.member;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>成员数据源</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface MemberRepository extends BaseRepository<MemberEntity> {
    /**
     * 根据用户和房间查询成员
     *
     * @param user 用户
     * @param room 房间
     * @return 成员，不存在时返回 {@code null}
     * @apiNote 传的是仅带 ID 的 {@code UserEntity} / {@code RoomEntity} 实例，依赖 JPA 按主键关联查询
     */
    MemberEntity getByUserAndRoom(UserEntity user, RoomEntity room);
}
