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
     * 根据邮箱查询一个用户
     *
     * @param email 邮箱
     * @return 用户
     */
    UserEntity getByEmail(String email);

    /**
     * 根据账号查询一个用户
     *
     * @param account 账号
     * @return 用户
     */
    UserEntity getByAccount(String account);

    /**
     * 根据手机查询一个用户
     *
     * @param phone 手机
     * @return 用户
     */
    UserEntity getByPhone(String phone);
}
