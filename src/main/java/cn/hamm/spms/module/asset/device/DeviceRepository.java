package cn.hamm.spms.module.asset.device;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>设备数据源</h1>
 *
 * @author zfy
 */
@Repository
public interface DeviceRepository extends BaseRepository<DeviceEntity> {
    /**
     * 通过 UUID 查询设备
     *
     * @param uuid UUID
     * @return 设备
     */
    DeviceEntity getByUuid(String uuid);
}
