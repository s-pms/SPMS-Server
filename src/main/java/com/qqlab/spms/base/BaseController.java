package com.qqlab.spms.base;

import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;

/**
 * <h1>实体控制器基类</h1>
 *
 * @param <S> Service
 * @param <E> 实体或实体的子类
 * @author Hamm
 */
@SuppressWarnings("rawtypes")
@Permission
public class BaseController<S extends BaseService, E extends BaseEntity<?>> extends RootEntityController<S, E> {
}