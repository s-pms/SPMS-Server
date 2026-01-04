package cn.hamm.spms.module.chat.room;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.util.annotation.ReadOnly;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

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
@Table(name = "room")
@Description("房间")
public class RoomEntity extends BaseEntity<RoomEntity> implements IRoomAction {
    @Description("房间号")
    @Column(nullable = false, columnDefinition = "int UNSIGNED comment '房间号'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "房间号不能为空")
    private Integer code;

    @Description("房间名称")
    @Column(nullable = false, columnDefinition = "varchar(255) default '' comment '房间名称'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class, WhenCreate.class}, message = "房间名称不能为空")
    private String name;

    @Description("房间简介")
    @Column(nullable = false, columnDefinition = "varchar(255) default '' comment '房间简介'")
    private String description;

    @Description("房间排序")
    @Column(nullable = false, columnDefinition = "int UNSIGNED default 0 comment '房间排序'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "房间排序不能为空")
    private Integer orderNumber;

    @Description("是否热门")
    @Column(columnDefinition = "bit(1) default 0 comment '是否热门'")
    private Boolean isHot;

    @Description("是否官方")
    @Column(columnDefinition = "bit(1) default 0 comment '是否官方'")
    private Boolean isOfficial;

    @Description("是否私有房间")
    @Column(columnDefinition = "bit(1) default 0 comment '是否私有房间'")
    private Boolean isPrivate;

    @Description("房间密码")
    @JsonProperty(access = WRITE_ONLY)
    @Column(nullable = false, columnDefinition = "varchar(255) default '' comment '房间密码'")
    private String password;

    @Description("房主信息")
    @ManyToOne
    @ReadOnly
    private UserEntity owner;

    @Description("房间大小")
    @Column(nullable = false, columnDefinition = "int UNSIGNED default 100000 comment '房间大小'")
    @Min(value = 0, message = "房间大小不小于{value}")
    @Max(value = 20 * 10000 * 10000, message = "房间大小不大于{value}")
    private Integer size;

    @Override
    public void excludeBaseData() {
        super.excludeBaseData();
        this.getOwner().excludeBaseData();
    }
}
