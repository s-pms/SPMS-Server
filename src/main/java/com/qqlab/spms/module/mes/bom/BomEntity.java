package com.qqlab.spms.module.mes.bom;


import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.airpower.annotation.Payload;
import cn.hamm.airpower.annotation.Search;
import com.qqlab.spms.base.bill.AbstractBaseBillEntity;
import com.qqlab.spms.module.asset.material.MaterialEntity;
import com.qqlab.spms.module.mes.bom.detail.BomDetailEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>BOM实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "bom")
@Description("BOM")
public class BomEntity extends AbstractBaseBillEntity<BomEntity, BomDetailEntity> {
    /**
     * 物料信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Payload
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "物料信息不能为空")
    private MaterialEntity material;

    @Description("BOM版本号")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment 'BOM版本号'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "BOM版本号不能为空")
    private String version;

    @Description("是否默认版本")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否默认版本'")
    @Exclude(filters = {WhenPayLoad.class})
    private Boolean defaultVersion;

    @Override
    public BomEntity setStatus(Integer status) {
        return null;
    }
}
