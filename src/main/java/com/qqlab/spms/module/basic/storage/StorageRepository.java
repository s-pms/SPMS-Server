package com.qqlab.spms.module.basic.storage;

import cn.hamm.airpower.root.RootRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface StorageRepository extends RootRepository<StorageEntity> {
}
