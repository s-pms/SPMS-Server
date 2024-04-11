package cn.hamm.spms.module.mes.craft;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.asset.material.MaterialEntity;
import cn.hamm.spms.module.channel.purchase.PurchaseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "craft_router")
@Description("工艺路线")
public class CraftRouterEntity extends BaseEntity<CraftRouterEntity> {
    @Payload
    @Description("物料")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料不能为空")
    private MaterialEntity material;

    @Description("产品工艺路线版本")
    @Search(Search.Mode.LIKE)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "产品工艺路线版本不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '产品工艺路线版本'", unique = true)
    private String routerVersion;

    @Description("工艺路线状态")
    @Search(Search.Mode.EQUALS)
    @Dictionary(value = PurchaseStatus.class,groups = {WhenAdd.class, WhenUpdate.class})
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '工艺路线状态'")
    private Integer status;

    @Description("是否默认版本")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否默认版本'")
    @Exclude(filters = {WhenPayLoad.class})
    private Boolean defaultVersion;


}
