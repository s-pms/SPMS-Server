package com.qqlab.spms.module.personnel.user;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface UserRepository extends BaseRepository<UserEntity> {
    /**
     * <h2>根据邮箱查询一个用户</h2>
     *
     * @param email 邮箱
     * @return 用户
     */
    UserEntity getByEmail(String email);

    /**
     * <h2>根据账号查询一个用户</h2>
     *
     * @param account 账号
     * @return 用户
     */
    UserEntity getByAccount(String account);

    /**
     * <h2>根据手机查询一个用户</h2>
     *
     * @param phone 手机
     * @return 用户
     */
    UserEntity getByPhone(String phone);
}
