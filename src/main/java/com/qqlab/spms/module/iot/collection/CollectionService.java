package com.qqlab.spms.module.iot.collection;

import com.qqlab.spms.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class CollectionService extends BaseService<CollectionEntity, CollectionRepository> {
    public CollectionEntity getDistinctFirstByUuidAndCode(String uuid, String code) {
        return repository.getDistinctFirstByUuidAndCodeOrderByIdDesc(uuid, code);
    }
}
