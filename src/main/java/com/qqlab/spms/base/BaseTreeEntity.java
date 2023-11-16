package com.qqlab.spms.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.interfaces.ITree;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * <h1>实体树基类</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@MappedSuperclass
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamicInsert
@DynamicUpdate
@Description("")
public class BaseTreeEntity<E extends BaseTreeEntity<E>> extends BaseEntity<E> implements ITree<E> {
    /**
     * <h2>树节点名称</h2>
     */
    @Description("名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @Length(max = 200, message = "名称最多允许{max}个字符")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "名称不能为空")
    private String name;
    /**
     * <h2>父级ID</h2>
     */
    @Description("父级ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    @Search(Search.Mode.EQUALS)
    private Long parentId;
    /**
     * <h2>树子集节点数组</h2>
     */
    @Transient
    private List<E> children;

    /**
     * <h2>设置名称</h2>
     *
     * @param name 名称
     * @return 实例
     */
    @Override
    public E setName(String name) {
        this.name = name;
        return (E) this;
    }

    /**
     * <h2>设置父级ID</h2>
     *
     * @param parentId 父级ID
     * @return 实例
     */
    @Override
    public E setParentId(Long parentId) {
        this.parentId = parentId;
        return (E) this;
    }

    @Override
    public E setChildren(List<E> children) {
        this.children = children;
        return (E) this;
    }
}
