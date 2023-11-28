package com.qqlab.spms.module.asset.device;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * @author zfy
 * @date 2023/11/28
 */
@Service
public class DeviceService extends BaseService<DeviceEntity, DeviceRepository> {

    @Override
    protected DeviceEntity beforeSaveToDatabase(DeviceEntity entity) {
        entity.setStatus(DeviceStatus.UNKNOWN.getValue());
        return super.beforeSaveToDatabase(entity);
    }
}
