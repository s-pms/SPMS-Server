package cn.hamm.spms.module.chat.member;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.chat.member.enums.MemberRole;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_EXIST;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class MemberService extends BaseService<MemberEntity, MemberRepository> {
    /**
     * 根据用户 ID 和房间 ID 获取成员信息
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @return 成员信息
     */
    public MemberEntity getMember(long userId, long roomId) {
        return repository.getByUserAndRoom(new UserEntity().setId(userId), new RoomEntity().setId(roomId));
    }

    /**
     * 添加成员
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @return 成员
     */
    public MemberEntity addMember(long userId, long roomId) {
        MemberEntity member = getMember(userId, roomId);
        FORBIDDEN_EXIST.whenNotNull(member);
        // 没有查到成员信息
        RoomEntity room = Services.getRoomService().get(roomId);
        member = new MemberEntity()
                .setUser(new UserEntity().setId(userId))
                .setRoom(new RoomEntity().setId(roomId));
        if (userId == room.getOwner().getId()) {
            // 是所有者
            member.setRole(MemberRole.OWNER.getKey());
        } else {
            member.setRole(MemberRole.VISITOR.getKey());
        }
        return addAndGet(member);
    }

    /**
     * 获取成员信息，若不存在则自动创建
     *
     * @param userId 用户 ID
     * @param roomId 房间 ID
     * @return 成员信息
     */
    public MemberEntity getMemberWithAutoCreate(long userId, long roomId) {
        MemberEntity member = getMember(userId, roomId);
        if (Objects.isNull(member)) {
            return addMember(userId, roomId);
        }
        return member;
    }
}
