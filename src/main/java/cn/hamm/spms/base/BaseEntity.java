package cn.hamm.spms.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;


/**
 * <h1>应用实体基类</h1>
 *
 * @author Hamm
 */
@MappedSuperclass
@Getter
@Description("")
public class BaseEntity<E extends BaseEntity<E>> extends RootEntity<E> {
}
