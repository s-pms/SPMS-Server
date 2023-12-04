package com.qqlab.spms.module.asset.device;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import com.qqlab.spms.module.iot.report.ReportData;
import com.qqlab.spms.module.iot.report.ReportEvent;
import com.qqlab.spms.module.iot.report.ReportPayload;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private ParameterService parameterService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询指定设备uuid的当前报告
     *
     * @param device 设备
     * @return 报告列表
     */
    public List<ReportPayload> getCurrentReport(DeviceEntity device) {
        device = getById(device.getId());
        Object data = redisTemplate.opsForValue().get(ReportEvent.PREFIX + device.getUuid());
        if (Objects.isNull(data)) {
            return new ArrayList<>();
        }
        return JSON.parseObject(data.toString(), ReportData.class).getPayloads();
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
