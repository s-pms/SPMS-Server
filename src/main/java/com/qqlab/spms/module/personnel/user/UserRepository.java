package com.qqlab.spms.module.personnel.user;

import cn.hamm.airpower.root.RootRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface UserRepository extends RootRepository<UserEntity> {
    /**
     * <h2>根据邮箱查询一个用户</h2>
     *
     * @param email 邮箱
     * @return 用户
     */
    UserEntity getByEmail(String email);
}
