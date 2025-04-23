package cn.hamm.spms.module.chat.member;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.chat.room.RoomEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;
import static cn.hamm.airpower.annotation.Search.Mode.JOIN;

/**
 * <h1>角色实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "member")
@Description("成员")
public class MemberEntity extends BaseEntity<MemberEntity> implements IMemberAction {
    @Description("用户信息")
    @ManyToOne
    @Search(JOIN)
    @ReadOnly
    private UserEntity user;

    @Description("房间信息")
    @ManyToOne
    @Search(JOIN)
    @ReadOnly
    private RoomEntity room;

    @Description("成员角色")
    @Dictionary(value = MemberRole.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 6 comment '类型'")
    @Search(EQUALS)
    private Integer role;

    @Override
    public void excludeBaseData() {
        super.excludeBaseData();
        this.getRoom().excludeBaseData();
        this.getUser().excludeBaseData();
    }
}
