package com.qqlab.spms.module.iot.collection;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 * @noinspection JpaQlInspection
 */
@Repository
public interface CollectionRepository extends BaseRepository<CollectionEntity> {
    /**
     * 查询UUID的去重数据
     *
     * @param uuid UUID
     * @param code Code
     * @return 数组
     */
    CollectionEntity getDistinctFirstByUuidAndCodeOrderByIdDesc(String uuid, String code);
}
