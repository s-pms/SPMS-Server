package com.qqlab.spms.module.asset.device;

import com.qqlab.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * @author zfy
 * @date 2023/11/28
 */
@Repository
public interface DeviceRepository extends BaseRepository<DeviceEntity> {
    /**
     * <h2>通过UUID查询设备</h2>
     *
     * @param uuid UUID
     * @return 设备
     */
    DeviceEntity getByUuid(String uuid);
}
