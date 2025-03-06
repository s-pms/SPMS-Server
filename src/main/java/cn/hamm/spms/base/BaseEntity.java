package cn.hamm.spms.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ExcelColumn;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.root.RootEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static cn.hamm.airpower.annotation.ExcelColumn.Type.BOOLEAN;
import static cn.hamm.airpower.annotation.Search.Mode.EQUALS;

/**
 * <h1>应用实体基类</h1>
 *
 * @author Hamm.cn
 */
@MappedSuperclass
@Getter
@DynamicInsert
@DynamicUpdate
@Description("")
public class BaseEntity<E extends BaseEntity<E>> extends RootEntity<E> {
    @Description("是否已发布")
    @ReadOnly
    @Search(EQUALS)
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否已发布'")
    @ExcelColumn(BOOLEAN)
    private Boolean isPublished;

    /**
     * <h3>设置是否已发布</h3>
     *
     * @param isPublished 是否已发布
     * @return 实体
     */
    public E setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
        //noinspection unchecked
        return (E) this;
    }
}
