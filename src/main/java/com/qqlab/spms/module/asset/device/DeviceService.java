package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.query.QueryRequest;
import cn.hutool.core.util.StrUtil;
import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.iot.collection.CollectionEntity;
import com.qqlab.spms.module.iot.collection.CollectionService;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zfy
 * @date 2023/11/28
 */
@Service
public class DeviceService extends BaseService<DeviceEntity, DeviceRepository> {
    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ParameterService parameterService;

    public List<CollectionEntity> getCurrentCollectionList(DeviceEntity device) {
        List<ParameterEntity> parameterList = parameterService.getList(new QueryRequest<>());
        device = getById(device.getId());
        List<CollectionEntity> result = new ArrayList<>();
        for (ParameterEntity parameter : parameterList) {
            CollectionEntity collection = collectionService.getDistinctFirstByUuidAndCode(device.getUuid(), parameter.getCode());
            if (Objects.isNull(collection)) {
                continue;
            }
            collection.setLabel(parameter.getLabel());
            result.add(collection);
        }
        return result;
    }

    /**
     * <h2>通过UUID查询设备</h2>
     *
     * @param uuid UUID
     * @return 设备
     */
    public DeviceEntity getByUuid(String uuid) {
        return repository.getByUuid(uuid);
    }

    @Override
    protected DeviceEntity beforeSaveToDatabase(DeviceEntity entity) {
        if (StrUtil.isBlank(entity.getCode())) {
            entity.setCode(createCode(CodeRuleField.DeviceCode));
        }
        if (StrUtil.isBlank(entity.getUuid())) {
            entity.setUuid(entity.getCode());
        }
        return super.beforeSaveToDatabase(entity);
    }

}
