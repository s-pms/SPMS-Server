package cn.hamm.spms.module.personnel.user;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <h1>用户角色关联</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface UserRoleLinkRepository extends BaseRepository<UserRoleLinkEntity> {

    /**
     * 按用户 ID 查关联记录
     *
     * @param userId 用户 ID
     * @return 关联记录
     */
    @Query("select l from UserRoleLinkEntity l where l.user.id = :userId")
    List<UserRoleLinkEntity> findByUserId(@Param("userId") Long userId);
}
