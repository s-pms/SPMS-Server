package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.open.notify.enums.NotifyChannel;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

/**
 * <h1>实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "notify")
@Description("通知")
public class NotifyEntity extends BaseEntity<NotifyEntity> {
    @Description("通知渠道")
    @Dictionary(value = NotifyChannel.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '通知渠道'")
    private Integer channel;

    @Description("通知场景")
    @Dictionary(value = NotifyScene.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '通知场景'")
    private Integer scene;

    @Description("通知地址")
    @Column(columnDefinition = "varchar(255) default '' comment '通知地址'")
    private String url;

    @Description("通知令牌")
    @Column(columnDefinition = "varchar(255) default '' comment '通知令牌'")
    private String token;

    @Description("备注信息")
    @Search
    @Column(columnDefinition = "text comment '备注信息'")
    @Length(max = 1000, message = "备注信息最多允许{max}个字符")
    private String remark;
}
