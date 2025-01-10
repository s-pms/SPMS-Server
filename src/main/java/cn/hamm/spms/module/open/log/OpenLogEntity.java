package cn.hamm.spms.module.open.log;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.open.app.OpenAppEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
@Table(name = "open_log")
@Description("调用日志")
public class OpenLogEntity extends BaseEntity<OpenLogEntity> implements IOpenLogAction {
    @Description("URL")
    @Column(columnDefinition = "varchar(255) default '' comment 'URL'")
    @ReadOnly
    private String url;

    @Description("来源IP")
    @Column(columnDefinition = "varchar(255) default '' comment '来源IP'")
    @ReadOnly
    private String ip;

    @Description("请求")
    @Column(columnDefinition = "text comment '请求'")
    @ReadOnly
    private String request;

    @Description("响应")
    @Column(columnDefinition = "text comment '响应'")
    @ReadOnly
    private String response;

    @Description("应用")
    @ManyToOne
    @Search(Search.Mode.JOIN)
    @ReadOnly
    private OpenAppEntity openApp;
}
