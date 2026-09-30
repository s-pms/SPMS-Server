package cn.hamm.spms.module.chat.room;

import cn.hamm.airpower.core.RandomUtil;
import cn.hamm.airpower.curd.model.query.Sort;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.chat.member.MemberEntity;
import cn.hamm.spms.module.chat.member.enums.MemberRole;
import cn.hamm.spms.module.personnel.PersonnelServices;
import cn.hamm.spms.module.personnel.user.UserEntity;
import io.micrometer.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>房间</h1>
 *
 * @author Hamm.cn
 */
@Service
public class RoomService extends BaseService<RoomEntity, RoomRepository> {
    /**
     * 单个用户允许创建的最大房间数量
     */
    private final static int MAX_ROOM_COUNT = 3;

    /**
     * 校验私有房间必须配密码
     *
     * @param room 房间
     * @return 房间
     * @apiNote 仅在 {@code isPrivate} 显式为 {@code true} 时校验，{@code null} 会被放行
     */
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
     * @param userId 房主 ID
     * @return 房间 ID
     * @apiNote 房间号是 6 位随机数且全局唯一，撞号时递归重试；重试前会重新统计房间数，
     * 因此该校验是「每次重试都跑一遍」而非只跑一次
     */
    public final long create(RoomEntity room, long userId) {
        RoomEntity filter = new RoomEntity().setOwner(new UserEntity().setId(userId));
        List<RoomEntity> list = filter(filter);
        PARAM_INVALID.when(list.size() >= MAX_ROOM_COUNT, "您最多只能创建" + MAX_ROOM_COUNT + "个房间");
        int code = RandomUtil.randomInt(100000, 999999);
        filter = new RoomEntity().setCode(code);
        list = filter(filter);
        if (!list.isEmpty()) {
            return create(room, userId);
        }
        room.setCode(code);
        UserEntity me = PersonnelServices.getUserService().get(userId);
        room.setOwner(me);
        room.setIsHot(false).setOrderNumber(0).setIsOfficial(false);
        return add(room);
    }

    /**
     * 获取热门房间
     *
     * @return 房间列表，按 {@code orderNumber} 倒序
     */
    public List<RoomEntity> getHotRoomList() {
        Sort sort = new Sort().setField("orderNumber").setDirection(Sort.DESC);
        return filter(new RoomEntity().setIsHot(true), sort);
    }

    /**
     * 根据房间号获取房间
     *
     * @param code 房间号
     * @return 房间，不存在时返回 {@code null}
     */
    public RoomEntity getByCode(int code) {
        return repository.getByCode(code);
    }

    /**
     * 检查成员进入私有房间是否需要密码
     *
     * @param member 成员
     * @return 仅 {@code MEMBER} 和 {@code VISITOR} 需要密码
     * @apiNote 房主与各管理员角色免密码，所以这个方法只适用于「已是该房间成员」的场景
     */
    public boolean checkIfNeedPassword(@NotNull MemberEntity member) {
        if (!member.getRoom().getIsPrivate()) {
            return false;
        }
        MemberRole[] roles = new MemberRole[]{MemberRole.MEMBER, MemberRole.VISITOR};
        return Arrays.stream(roles).anyMatch(role -> role.equalsKey(member.getRole()));
    }
}
