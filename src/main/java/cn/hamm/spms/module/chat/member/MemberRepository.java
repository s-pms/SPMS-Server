package cn.hamm.spms.module.chat.member;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.springframework.stereotype.Repository;

/**
 * <h1>Repository</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface MemberRepository extends BaseRepository<MemberEntity> {
    /**
     * <h3>根据用户和房间查询</h3>
     *
     * @param user 用户
     * @param room 房间
     * @return 成员
     */
    MemberEntity getByUserAndRoom(UserEntity user, RoomEntity room);
}
