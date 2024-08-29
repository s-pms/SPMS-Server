package cn.hamm.spms.module.webhook;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.webhook.enums.WebHookScene;
import cn.hamm.spms.module.webhook.enums.WebHookType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@Table(name = "webhook")
@Description("通知钩子")
public class WebHookEntity extends BaseEntity<WebHookEntity> {
    @Description("类型")
    @Dictionary(value = WebHookType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '类型'")
    @Search(Search.Mode.EQUALS)
    private Integer type;

    @Description("场景")
    @Dictionary(value = WebHookScene.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '场景'")
    @Search(Search.Mode.EQUALS)
    private Integer scene;

    @Description("网址")
    @Column(columnDefinition = "varchar(255) default '' comment '网址'")
    private String url;

    @Description("令牌")
    @Column(columnDefinition = "varchar(255) default '' comment '令牌'")
    private String token;
}
