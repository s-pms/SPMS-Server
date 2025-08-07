package cn.hamm.spms.module.chat.room;

import cn.hamm.airpower.curd.Sort;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.chat.member.MemberEntity;
import cn.hamm.spms.module.chat.member.enums.MemberRole;
import cn.hamm.spms.module.personnel.user.UserEntity;
import io.micrometer.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class RoomService extends BaseService<RoomEntity, RoomRepository> {
    /**
     * 允许用户创建的最大房间数量
     */
    private final static int MAX_ROOM_COUNT = 3;

    @Override
    protected @NotNull RoomEntity beforeAppSaveToDatabase(@NotNull RoomEntity room) {
        PARAM_INVALID.when(
                Objects.nonNull(room.getIsPrivate()) &&
                        room.getIsPrivate() &&
                        StringUtils.isEmpty(room.getPassword()),
                "私有房间必须设置密码");
        return room;
    }

    /**
     * 创建房间
     *
     * @param room   房间对象
     * @param userId 房主ID
     * @return 房间ID
     */
    public final long create(RoomEntity room, long userId) {
        RoomEntity filter = new RoomEntity().setOwner(new UserEntity().setId(userId));
        List<RoomEntity> list = filter(filter);
        PARAM_INVALID.when(list.size() >= MAX_ROOM_COUNT, "您最多只能创建" + MAX_ROOM_COUNT + "个房间");
        int code = RandomUtil.randomInt(100000, 999999);
        filter = new RoomEntity().setCode(code);
        list = filter(filter);
        if (!list.isEmpty()) {
            // 递归创建 此处需要注意后续优化
            return create(room, userId);
        }
        // code 没有被使用
        room.setCode(code);
        UserEntity me = Services.getUserService().get(userId);
        room.setOwner(me);
        room.setIsHot(false).setOrderNumber(0).setIsOfficial(false);
        return add(room);
    }

    /**
     * 获取热门房间
     *
     * @return 房间列表
     */
    public List<RoomEntity> getHotRoomList() {
        Sort sort = new Sort().setField("orderNumber").setDirection(Sort.DESC);
        return filter(new RoomEntity().setIsHot(true), sort);
    }

    /**
     * 根据房间号获取房间
     *
     * @param code 房间号
     * @return 房间
     */
    public RoomEntity getByCode(int code) {
        return repository.getByCode(code);
    }

    /**
     * 检查是否需要密码
     *
     * @param member 成员
     * @return 是否需要密码
     */
    public boolean checkIfNeedPassword(@NotNull MemberEntity member) {
        if (!member.getRoom().getIsPrivate()) {
            return false;
        }
        // 需要密码的角色
        MemberRole[] roles = new MemberRole[]{MemberRole.MEMBER, MemberRole.VISITOR};
        return Arrays.stream(roles).anyMatch(role -> role.equalsKey(member.getRole()));
    }
}
