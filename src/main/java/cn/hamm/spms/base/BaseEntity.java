package cn.hamm.spms.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


/**
 * <h1>应用实体基类</h1>
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
public class BaseEntity<E extends BaseEntity<E>> extends RootEntity<E> {
}
