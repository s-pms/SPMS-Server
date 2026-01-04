package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Desensitize;
import cn.hamm.airpower.core.enums.DesensitizeType;
import cn.hamm.airpower.web.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.EAGER;

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
@Table(name = "personal_token")
@Description("私人令牌")
public class PersonalTokenEntity extends BaseEntity<PersonalTokenEntity> {
    @Description("令牌名称")
    @Column(columnDefinition = "varchar(255) default '' comment '令牌名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "令牌名称不能为空")
    @Search
    private String name;

    @Description("令牌")
    @Column(columnDefinition = "varchar(255) default '' comment '令牌'", unique = true)
    @Desensitize(value = DesensitizeType.CUSTOM, tail = 2, head = 6, replace = true, symbol = "**********")
    private String token;

    @Description("所属用户")
    @ManyToOne(fetch = EAGER)
    private UserEntity user;
}
